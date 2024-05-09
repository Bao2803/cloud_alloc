package westwood222.cloud_alloc.service.manager;

import westwood222.cloud_alloc.service.CloudService;

/**
 * This class is used to keep a reference of all service accounts.
 * It can be used to retrieve the best service for an operation.
 */
public interface ServiceManager {
    CloudService poll();

    /**
     * Add a new service to the collection.
     * @param service service to be added
     * @return true if success
     */
    boolean add(CloudService service);
}
