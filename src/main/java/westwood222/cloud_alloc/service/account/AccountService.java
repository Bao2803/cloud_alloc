package westwood222.cloud_alloc.service.manager;

import westwood222.cloud_alloc.service.storage.CloudStorageService;

/**
 * This class is used to keep a reference of all service accounts.
 * It can be used to retrieve the best service for an operation.
 */
public interface ManagerService {
    /**
     * Get the best service for uploading (use available space)
     *
     * @return CloudService that have the largest available space.
     */
    CloudStorageService getMaxSpace();

    /**
     * Get the service that contains a particular file/document
     * @param fileId unique identifier that identify a file/folder
     * @return CloudService that contains a resource specify by fileId
     */
    CloudStorageService getContainer(String fileId);

    /**
     * Add a new service to the collection.
     * @param service service to be added
     * @return true if success
     */
    boolean add(CloudStorageService service);
}
