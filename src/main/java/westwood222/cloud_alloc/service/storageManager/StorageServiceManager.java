package westwood222.cloud_alloc.service.account;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.service.storage.StorageService;
import westwood222.cloud_alloc.service.storage.GoogleStorageService;
import westwood222.cloud_alloc.service.storage.StorageService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * This service managing all the cloud storage account provided by the user.
 * It interacts directly with the {@link westwood222.cloud_alloc.repository.ResourceRepository} to keep track of
 * the users' storage accounts metadata.
 * It also in charge of managing all StorageService instances during runtime.
 */
public interface AccountService extends AuthenticationSuccessHandler {
    /**
     * Create a new instance of StorageService based on the input account.
     *
     * @param account contains information for OAuth2.0
     * @return StorageService that holds the accessToken to the input account
     */
    static StorageService createStorageService(Account account) throws IOException {
        return switch (account.getProvider()) {
            case google -> GoogleStorageService.createInstance(account);
            case microsoft, dropbox -> throw new RuntimeException("Not implemented");
        };
    }

    @Nonnull
    StorageService getBestFit(long spaceNeed);

    /**
     * Add a service corresponding to an account back to the internal PriorityQueue.
     *
     * @param service serv
     * @return true if success
     * @throws Exception if fail
     */
    boolean add(StorageService service) throws Exception;

    /**
     * Get the service corresponding to the input id
     *
     * @param id of the target StorageService
     * @return CloudService with the input id if one exists, Optional.empty() otherwise
     */
    Optional<StorageService> getById(UUID id);
}
