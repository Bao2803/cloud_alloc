package westwood222.cloud_alloc.service.storage;

import westwood222.cloud_alloc.dto.delete.DeleteRequest;
import westwood222.cloud_alloc.dto.upload.UploadRequest;
import westwood222.cloud_alloc.dto.upload.UploadResponse;
import westwood222.cloud_alloc.dto.view.ViewRequest;
import westwood222.cloud_alloc.dto.view.ViewResponse;

/**
 * Interface for all cloud storage provider services.
 * MUST IMPLEMENT and addition {@code createInstance()} method.
 * This method will be used through Java reflection in the AccountServiceConfig class.
 */
public interface StorageService {
    long freeSpace() throws Exception;

    UploadResponse upload(UploadRequest request) throws Exception;

    ViewResponse view(ViewRequest request) throws Exception;

    void delete(DeleteRequest request) throws Exception;
}
