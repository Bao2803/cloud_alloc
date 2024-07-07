package westwood222.cloud_alloc.repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;
import westwood222.cloud_alloc.dto.storage.worker.delete.WorkerDeleteRequest;
import westwood222.cloud_alloc.dto.storage.worker.delete.WorkerDeleteResponse;
import westwood222.cloud_alloc.dto.storage.worker.read.WorkerReadRequest;
import westwood222.cloud_alloc.dto.storage.worker.read.WorkerReadResponse;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadRequest;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadResponse;
import westwood222.cloud_alloc.exception.internal.AccountNotFound;
import westwood222.cloud_alloc.exception.internal.InsufficientStorage;
import westwood222.cloud_alloc.mapper.StorageMapper;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.Provider;
import westwood222.cloud_alloc.oauth.OAuthProperty;
import westwood222.cloud_alloc.service.storage.worker.GoogleStorageWorker;
import westwood222.cloud_alloc.service.storage.worker.StorageWorker;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StorageWorkerRepositoryImpl implements StorageWorkerRepository {
    private final StorageMapper storageMapper;
    private final OAuthProperty oAuthProperty;
    private final AccountRepository accountRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${spring.application.core.min-size-default}")
    private long MINIMUM_BYTE;
    private Map<UUID, StorageWorker> serviceMap;
    private TreeSet<StorageWorker> serviceTreeSet;

    @PostConstruct
    private void createStorageServices() {
        List<Account> accounts = accountRepository.findAll();
        this.serviceMap = createStorageServiceMap(accounts, storageMapper);
        this.serviceTreeSet = createStorageServiceTreeSet(serviceMap.values());
    }

    /**
     * Save/update all account metadata in DB. The primary focus is updating account's free space
     */
    @PreDestroy
    private void destroy() {
        Account account;
        for (StorageWorker service : serviceMap.values()) {
            account = service.getAccount();
            account.setAvailableSpace(service.getFreeSpace());
            accountRepository.save(account);
        }
    }

    /**
     * Create a new instance of StorageService based on the input account.
     *
     * @param account contains information for OAuth2.0
     * @return StorageService that holds the accessToken to the input account
     */
    private StorageWorker createStorageService(
            Account account,
            StorageMapper storageMapper
    ) throws IOException {
        Provider provider = account.getProvider();
        return switch (provider) {
            case GOOGLE -> {
                OAuthProperty.ProviderSecret googleSecret = oAuthProperty.getProviderSecret(provider);
                yield new GoogleStorageWorker(account, googleSecret, storageMapper);
            }
            case MICROSOFT, DROPBOX -> throw new RuntimeException("Not implemented");
        };
    }

    private Map<UUID, StorageWorker> createStorageServiceMap(
            List<Account> accounts,
            StorageMapper storageMapper
    ) {
        Map<UUID, StorageWorker> storageServiceMap = new HashMap<>(accounts.size());

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

    private TreeSet<StorageWorker> createStorageServiceTreeSet(
            Collection<StorageWorker> services
    ) {
        TreeSet<StorageWorker> treeSet = new TreeSet<>(
                Comparator.comparingLong(StorageWorker::getFreeSpace)
        );
        treeSet.addAll(services);
        return treeSet;
    }

    private StorageWorker createQueryObject(long spaceNeed) {
        spaceNeed = spaceNeed <= 0 ? MINIMUM_BYTE : spaceNeed;
        return new StorageWorker(new Account(), spaceNeed) {
            @Override
            public long getFreeSpace() {
                return this.freeSpace;
            }

            @Override
            public WorkerUploadResponse upload(WorkerUploadRequest request) {
                return null;
            }

            @Override
            public WorkerReadResponse read(WorkerReadRequest request) {
                return null;
            }

            @Override
            public WorkerDeleteResponse delete(WorkerDeleteRequest request) {
                return null;
            }


        };
    }

    @Nonnull
    public StorageWorker getServiceBySpace(long spaceNeed) throws InsufficientStorage {
        StorageWorker queryObject = createQueryObject(spaceNeed);

        StorageWorker service = serviceTreeSet.higher(queryObject);
        try {
            if (service == null || service.getFreeSpace() <= spaceNeed) {
                throw new InsufficientStorage(spaceNeed);
            }
        } catch (InsufficientStorage ie) {
            throw ie;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        serviceTreeSet.remove(service);
        return service;
    }

    @Nonnull
    public StorageWorker getServiceById(UUID id) throws AccountNotFound {
        StorageWorker storageService = serviceMap.get(id);
        if (storageService == null) {
            throw new AccountNotFound(id);
        }
        serviceTreeSet.remove(storageService);
        return storageService;
    }

    public void addService(StorageWorker storageService) {
        serviceTreeSet.add(storageService);
        serviceMap.putIfAbsent(storageService.getAccount().getId(), storageService);
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
        StorageWorker service = createStorageService(account, storageMapper);
        account.setAvailableSpace(service.getFreeSpace());

        // Keep track of the new driveService
        serviceTreeSet.add(service);
        serviceMap.put(account.getId(), service);
        accountRepository.save(account);

        // Redirect to a success page or handle the response as needed
        response.sendRedirect("/home");
    }
}
