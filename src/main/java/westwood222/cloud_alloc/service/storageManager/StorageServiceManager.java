package westwood222.cloud_alloc.service.storageManager;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import westwood222.cloud_alloc.exception.internal.AccountNotFound;
import westwood222.cloud_alloc.exception.internal.InsufficientStorage;
import westwood222.cloud_alloc.service.storage.AbstractStorageService;
import westwood222.cloud_alloc.service.storage.StorageService;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * This class manage all the cloud storage account provided by the user.
 * It interacts directly with the {@link westwood222.cloud_alloc.repository.ResourceRepository} to keep track of
 * the users' storage accounts metadata.
 * It also in charge of instantiate all StorageService instances during startup.
 */
public interface StorageServiceManager extends StorageService, AuthenticationSuccessHandler {

    /**
     * Add/return a driveService.
     *
     * @param service "rented"/new driveService that will be managed by the manager
     * @return true if success
     */
    boolean add(@Nonnull AbstractStorageService service);

    /**
     * Get a StorageService for uploading file with {@code spaceNeed}. The algorithm of which driveService to be used is
     * implementation-specific.
     *
     * @param spaceNeed the space of the uploaded file
     * @return a driveService that can be used to upload file.
     * Caller MUST RETURN the driveService to the manager using by calling {@link StorageServiceManager#add(AbstractStorageService)}
     */
    @Nonnull
    AbstractStorageService getServiceBySpace(long spaceNeed) throws InsufficientStorage;

    /**
     * Get the driveService corresponding to the input id.
     *
     * @param id of the target StorageService
     * @return CloudService with the input id if one exists, Optional.empty() otherwise.
     * Caller MUST RETURN the driveService to the manager using by calling {@link StorageServiceManager#add(AbstractStorageService)}
     */
    AbstractStorageService getServiceById(UUID id) throws AccountNotFound;
}
