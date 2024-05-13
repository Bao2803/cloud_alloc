package westwood222.cloud_alloc.service.account;

import lombok.NonNull;
import westwood222.cloud_alloc.service.storage.StorageService;

import java.util.Optional;
import java.util.UUID;

/**
 * This service managing all the cloud storage account provided by the user.
 * It interacts directly with the {@link westwood222.cloud_alloc.repository.ResourceRepository} to keep track of
 * the users' storage accounts metadata.
 * It also in charge of managing all StorageService instances during runtime.
 */
public interface AccountService {
    /**
     * Get the best service for uploading (use available space)
     *
     * @param spaceNeed the space needed for a new file upload. 0 or -1 if unknown.
     * @return CloudService that have the largest available space.
     */
    @NonNull
    StorageService getMaxSpace(long spaceNeed);

    /**
     * Add a new account (Each account is represented by a StorageService)
     *
     * @return true if success
     * @throws Exception if fail
     */
    boolean newAccount() throws Exception;

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
