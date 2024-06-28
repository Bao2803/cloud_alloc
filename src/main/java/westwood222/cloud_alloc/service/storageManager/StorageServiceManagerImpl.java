package westwood222.cloud_alloc.service.storageManager;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteRequest;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteResponse;
import westwood222.cloud_alloc.dto.storage.read.StorageReadRequest;
import westwood222.cloud_alloc.dto.storage.read.StorageReadResponse;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadRequest;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadResponse;
import westwood222.cloud_alloc.exception.internal.AccountNotFound;
import westwood222.cloud_alloc.exception.internal.InsufficientStorage;
import westwood222.cloud_alloc.mapper.StorageMapper;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.Provider;
import westwood222.cloud_alloc.oauth.OAuthProperty;
import westwood222.cloud_alloc.repository.AccountRepository;
import westwood222.cloud_alloc.service.storage.AbstractStorageService;
import westwood222.cloud_alloc.service.storage.GoogleStorageService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceManagerImpl implements StorageServiceManager {
    private final OAuthProperty oAuthProperty;
    private final StorageMapper storageMapper;
    private final AccountRepository accountRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.application.service.account.min-size-default}")
    private long MINIMUM_BYTE;
    private Map<UUID, AbstractStorageService> serviceMap;
    private TreeSet<AbstractStorageService> serviceTreeSet;

    @PostConstruct
    private void createStorageServices() {
        List<Account> accounts = accountRepository.findAll();
        this.serviceMap = createStorageServiceMap(accounts);
        this.serviceTreeSet = createStorageServiceTreeSet(serviceMap.values());
    }

    /**
     * Create a new instance of StorageService based on the input account.
     *
     * @param account contains information for OAuth2.0
     * @return StorageService that holds the accessToken to the input account
     */
    private AbstractStorageService createStorageService(
            Account account,
            StorageMapper storageMapper
    ) throws IOException {
        Provider provider = account.getProvider();
        Map<String, OAuthProperty.ProviderSecret> registeredProvider = oAuthProperty.getRegistration();
        return switch (provider) {
            case google -> {
                OAuthProperty.ProviderSecret googleSecret = registeredProvider.get(provider.name());
                yield new GoogleStorageService(account, googleSecret, storageMapper);
            }
            case microsoft, dropbox -> throw new RuntimeException("Not implemented");
        };
    }

    private Map<UUID, AbstractStorageService> createStorageServiceMap(
            List<Account> accounts
    ) {
        Map<UUID, AbstractStorageService> storageServiceMap = new HashMap<>(accounts.size());

        for (Account account : accounts) {
            try {
                storageServiceMap.put(
                        account.getId(),
                        createStorageService(account, storageMapper)
                );
            } catch (IOException e) {
                log.error("Can't instantiate driveService for account: {}", account, e);
            }
        }

        return storageServiceMap;
    }

    private TreeSet<AbstractStorageService> createStorageServiceTreeSet(Collection<AbstractStorageService> services) {
        TreeSet<AbstractStorageService> treeSet = new TreeSet<>(Comparator.comparingLong(AbstractStorageService::getFreeSpace));
        treeSet.addAll(services);
        return treeSet;
    }

    @Override
    public @NonNull AbstractStorageService getServiceBySpace(long spaceNeed) throws InsufficientStorage {
        AbstractStorageService queryObject = createQueryObject(spaceNeed);

        AbstractStorageService service = serviceTreeSet.higher(queryObject);
        try {
            if (service == null || service.getFreeSpace() <= spaceNeed) {
                throw new InsufficientStorage(String.format("Insufficient storage to upload file with %d", spaceNeed));
            }
        } catch (InsufficientStorage ie) {
            throw ie;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        serviceTreeSet.remove(service);
        return service;
    }

    private AbstractStorageService createQueryObject(long spaceNeed) {
        spaceNeed = spaceNeed <= 0 ? MINIMUM_BYTE : spaceNeed;
        return new AbstractStorageService(new Account(), spaceNeed) {
            @Override
            public StorageUploadResponse upload(StorageUploadRequest request) {
                return null;
            }

            @Override
            public StorageReadResponse read(StorageReadRequest request) {
                return null;
            }

            @Override
            public StorageDeleteResponse delete(StorageDeleteRequest request) {
                return null;
            }
        };
    }

    @Override
    public boolean add(@NonNull AbstractStorageService service) {
        return serviceTreeSet.add(service);
    }

    @Override
    public @Nonnull AbstractStorageService getServiceById(UUID id) throws AccountNotFound {
        AbstractStorageService storageService = serviceMap.get(id);
        if (storageService == null) {
            throw new AccountNotFound("No account with id " + id);
        }
        serviceTreeSet.remove(storageService);
        return storageService;
    }

    /**
     * Save/update all account metadata in DB. The primary focus is updating account's free space
     */
    @PreDestroy
    private void destroy() {
        Account account;
        for (AbstractStorageService service : serviceMap.values()) {
            account = service.getAccount();
            account.setAvailableSpace(service.getFreeSpace());
            accountRepository.save(account);
        }
    }

    /**
     * {@inheritDoc} Using the Authentication object to obtains the access and refresh token.
     * Then,
     * the access and refresh token is used
     * to create a new storage driveService for the application.
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2AuthenticationToken auth2AuthenticationToken = ((OAuth2AuthenticationToken) authentication);

        // Extract an object that holds access and refresh token
        String clientRegistrationId = auth2AuthenticationToken.getAuthorizedClientRegistrationId();
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                clientRegistrationId,
                authentication.getName()
        );

        Assert.notNull(
                authorizedClient.getRefreshToken(),
                "Couldn't get refresh token for " + clientRegistrationId
        );

        // Calculate refresh token expiration time
        Instant refreshTokenExpiredInstant = authorizedClient.getRefreshToken().getExpiresAt();
        LocalDateTime refreshTokenExpiredDateTime;
        if (refreshTokenExpiredInstant != null) {
            refreshTokenExpiredDateTime = LocalDateTime.ofInstant(refreshTokenExpiredInstant, ZoneId.systemDefault());
        } else {
            refreshTokenExpiredDateTime = LocalDateTime.now().plusYears(1000);  // PSQL can't handle LocalDateTime.MAX
        }

        // Get email
        OAuth2User user = auth2AuthenticationToken.getPrincipal();
        String email = user.getAttribute("email");
        Assert.notNull(
                email,
                "Couldn't get email for " + clientRegistrationId
        );

        // Construct an account object based on the authentication details
        Account account = Account.builder()
                .provider(Provider.getProvider(clientRegistrationId))
                .accessToken(authorizedClient.getAccessToken().getTokenValue())
                .refreshToken(authorizedClient.getRefreshToken().getTokenValue())
                .username(email)
                .expirationDateTime(refreshTokenExpiredDateTime)
                .build();

        // Initiate new driveService; update account's free space according to driveService's free space
        AbstractStorageService service = createStorageService(account, storageMapper);
        account.setAvailableSpace(service.getFreeSpace());

        // Keep track of the new driveService
        add(service);
        serviceMap.put(account.getId(), service);
        accountRepository.save(account);

        // Redirect to a success page or handle the response as needed
        response.sendRedirect("/home");
    }

    @Override
    public StorageUploadResponse upload(StorageUploadRequest request) {
        MultipartFile file = request.getFile();
        AbstractStorageService storageService = getServiceBySpace(file.getSize());
        try {
            StorageUploadRequest storageRequest = storageMapper.toStorageUploadRequest(file);
            return storageService.upload(storageRequest);
        } finally {
            this.serviceTreeSet.add(storageService);
        }
    }

    @Override
    public StorageReadResponse read(StorageReadRequest request) {
        AbstractStorageService service = getServiceById(request.getAccountId());
        try {
            StorageReadRequest storageRequest = storageMapper.toStorageReadRequest(
                    null,
                    request.getForeignId()
            );
            return service.read(storageRequest);
        } finally {
            this.serviceTreeSet.add(service);
        }
    }

    @Override
    public StorageDeleteResponse delete(StorageDeleteRequest request) {
        AbstractStorageService service = getServiceById(request.getAccountId());
        try {
            StorageDeleteRequest storageRequest = storageMapper.toStorageDeleteRequest(
                    null,
                    request.getForeignId(),
                    request.isHardDelete()
            );

            return service.delete(storageRequest);
        } finally {
            this.serviceTreeSet.add(service);
        }
    }
}
