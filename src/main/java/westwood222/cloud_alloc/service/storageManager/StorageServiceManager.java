package westwood222.cloud_alloc.service.storageManager;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import westwood222.cloud_alloc.exception.internal.AccountNotFound;
import westwood222.cloud_alloc.exception.internal.InsufficientStorage;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.service.storage.GoogleStorageService;
import westwood222.cloud_alloc.service.storage.StorageService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;

/**
 * This class manage all the cloud storage account provided by the user.
 * It interacts directly with the {@link westwood222.cloud_alloc.repository.ResourceRepository} to keep track of
 * the users' storage accounts metadata.
 * It also in charge of instantiate all StorageService instances during startup.
 */
public interface StorageServiceManager extends AuthenticationSuccessHandler {
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

    /**
     * Add/return a service.
     *
     * @param service "rented"/new service that will be managed by the manager
     * @return true if success
     */
    boolean add(@Nonnull StorageService service);

    /**
     * Get a StorageService for uploading file with {@code spaceNeed}. The algorithm of which service to be used is
     * implementation specific.
     *
     * @param spaceNeed the space of the uploaded file
     * @return a service that can be used to upload file.
     * Caller MUST RETURN the service to the manager using by calling {@link StorageServiceManager#add(StorageService)}
     */
    @Nonnull
    StorageService getServiceBySpace(long spaceNeed) throws InsufficientStorage;

    /**
     * Get the service corresponding to the input id.
     *
     * @param id of the target StorageService
     * @return CloudService with the input id if one exists, Optional.empty() otherwise.
     * Caller MUST RETURN the service to the manager using by calling {@link StorageServiceManager#add(StorageService)}
     */
    StorageService getServiceById(UUID id) throws AccountNotFound;
}
