package westwood222.cloud_alloc.service.storageManager;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import westwood222.cloud_alloc.exception.internal.AccountNotFound;
import westwood222.cloud_alloc.exception.internal.InsufficientStorage;
import westwood222.cloud_alloc.mapper.StorageMapper;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.Provider;
import westwood222.cloud_alloc.oauth.OAuthProperty;
import westwood222.cloud_alloc.repository.AccountRepository;
import westwood222.cloud_alloc.service.storage.StorageService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class StorageServiceManagerImpl implements StorageServiceManager {
    private final long MINIMUM_SPACE;   // in bytes
    private final OAuthProperty property;
    private final StorageMapper storageMapper;
    private final AccountRepository accountRepository;
    private final Map<UUID, StorageService> serviceMap;
    private final TreeSet<StorageService> serviceTreeSet;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    public StorageServiceManagerImpl(
            @Value("${spring.application.service.account.min-size-default}") int minSpace,
            AccountRepository accountRepository,
            OAuth2AuthorizedClientService authorizedClientService,
            OAuthProperty property,
            StorageMapper storageMapper
    ) {
        this.property = property;
        this.MINIMUM_SPACE = minSpace;
        this.accountRepository = accountRepository;
        this.authorizedClientService = authorizedClientService;
        this.storageMapper = storageMapper;

        List<Account> accounts = accountRepository.findAll();
        this.serviceMap = createStorageServiceMap(accounts);
        this.serviceTreeSet = createStorageServiceTreeSet(serviceMap.values());
    }

    private Map<UUID, StorageService> createStorageServiceMap(
            List<Account> accounts
    ) {
        Map<UUID, StorageService> storageServiceMap = new HashMap<>(accounts.size());

        for (Account account : accounts) {
            try {
                storageServiceMap.put(
                        account.getId(),
                        StorageServiceManager.createStorageService(account, property, storageMapper)
                );
            } catch (IOException e) {
                log.debug("Can't instantiate service for account: {}", account, e);
            }
        }

        return storageServiceMap;
    }

    private TreeSet<StorageService> createStorageServiceTreeSet(Collection<StorageService> services) {
        TreeSet<StorageService> treeSet = new TreeSet<>(Comparator.comparingLong(StorageService::getFreeSpace));
        treeSet.addAll(services);
        return treeSet;
    }

    @Override
    public @NonNull StorageService getServiceBySpace(long spaceNeed) throws InsufficientStorage {
        StorageService queryObject = createQueryObject(spaceNeed);

        StorageService service = serviceTreeSet.higher(queryObject);
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

    private StorageService createQueryObject(long spaceNeed) {
        spaceNeed = spaceNeed <= 0 ? MINIMUM_SPACE : spaceNeed;
        return StorageService.createInstance(spaceNeed);
    }

    @Override
    public boolean add(@NonNull StorageService service) {
        return serviceTreeSet.add(service);
    }

    @Override
    public @Nonnull StorageService getServiceById(UUID id) throws AccountNotFound {
        StorageService storageService = serviceMap.get(id);
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
        for (StorageService service : serviceMap.values()) {
            account = service.getAccount();
            account.setAvailableSpace(service.getFreeSpace());
            accountRepository.save(account);
        }
    }

    /**
     * {@inheritDoc} Using the Authentication object to obtains the access and refresh token.
     * Then, the access and refresh token is used to create a new storage service for the application.
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2AuthenticationToken auth2AuthenticationToken = ((OAuth2AuthenticationToken) authentication);

        // Extract object that holds access and refresh token
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
            refreshTokenExpiredDateTime = LocalDateTime.now().plusYears(1000);  // Postgres cannot handle LocalDateTime.MAX
        }

        // Get email
        OAuth2User user = auth2AuthenticationToken.getPrincipal();
        String email = user.getAttribute("email");
        Assert.notNull(
                email,
                "Couldn't get email for " + clientRegistrationId
        );

        // Construct account object based on the authentication details
        Account account = Account.builder()
                .provider(Provider.getProvider(clientRegistrationId))
                .accessToken(authorizedClient.getAccessToken().getTokenValue())
                .refreshToken(authorizedClient.getRefreshToken().getTokenValue())
                .username(email)
                .expirationDateTime(refreshTokenExpiredDateTime)
                .build();

        // Initiate new service; update account's free space according to service's free space
        StorageService service = StorageServiceManager.createStorageService(account, property, storageMapper);
        account.setAvailableSpace(service.getFreeSpace());

        // Keep track of the new service
        add(service);
        accountRepository.save(account);

        // Redirect to a success page or handle the response as needed
        response.sendRedirect("/home");
    }
}
