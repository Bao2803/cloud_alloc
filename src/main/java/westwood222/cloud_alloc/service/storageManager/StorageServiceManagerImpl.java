package westwood222.cloud_alloc.service.account;

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
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import westwood222.cloud_alloc.exception.internal.InsufficientStorage;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.Provider;
import westwood222.cloud_alloc.repository.AccountRepository;
import westwood222.cloud_alloc.service.storage.StorageService;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {
    private final long MINIMUM_SPACE;   // in bytes
    private final Map<UUID, StorageService> serviceMap;
    private final AccountRepository accountRepository;
    private final TreeSet<StorageService> serviceTreeSet;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    public AccountServiceImpl(
            AccountRepository accountRepository,
            OAuth2AuthorizedClientService authorizedClientService,
            @Value("${spring.application.service.account.min-size-default}") int minSpace
    ) {
        this.MINIMUM_SPACE = minSpace;
        this.accountRepository = accountRepository;
        this.authorizedClientService = authorizedClientService;

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
                        AccountService.createStorageService(account)
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
    public @NonNull StorageService getBestFit(long spaceNeed) {
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
    public boolean add(StorageService service) {
        return serviceTreeSet.add(service);
    }

    @Override
    public Optional<StorageService> getById(UUID id) {
        return Optional.of(serviceMap.get(id));
    }

    /**
     * Called when a user has been successfully authenticated.
     *
     * @param request        the request which caused the successful authentication
     * @param response       the response
     * @param authentication the <tt>Authentication</tt> object which was created during
     *                       the authentication process.
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        String clientRegistrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        // Extract the principal (user information)
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                clientRegistrationId,
                authentication.getName()
        );

        Assert.notNull(
                authorizedClient.getRefreshToken(),
                "Couldn't get refresh token for " + clientRegistrationId
        );

        Account account = Account.builder()
                .provider(Provider.getProvider(clientRegistrationId))
                .accessToken(authorizedClient.getAccessToken().getTokenValue())
                .refreshToken(authorizedClient.getRefreshToken().getTokenValue())
                .clientRegistration(authorizedClient.getClientRegistration())
                .build();
        add(AccountService.createStorageService(account));
        accountRepository.save(account);

        // Redirect to a success page or handle the response as needed
        response.sendRedirect("/home");
    }
}
