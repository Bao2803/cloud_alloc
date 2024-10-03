package westwood222.cloud_alloc.service.resource;

import westwood222.cloud_alloc.dto.resource.delete.ResourceDeleteRequest;
import westwood222.cloud_alloc.dto.resource.delete.ResourceDeleteResponse;
import westwood222.cloud_alloc.dto.resource.read.ResourceReadRequest;
import westwood222.cloud_alloc.dto.resource.read.ResourceReadResponse;
import westwood222.cloud_alloc.dto.resource.search.ResourceSearchRequest;
import westwood222.cloud_alloc.dto.resource.search.ResourceSearchResponse;
import westwood222.cloud_alloc.dto.resource.upload.ResourceUploadRequest;
import westwood222.cloud_alloc.dto.resource.upload.ResourceUploadResponse;
import westwood222.cloud_alloc.exception.external.ExternalException;
import westwood222.cloud_alloc.service.storage.manager.StorageManager;

/**
 * This service handle the resources (files and folders).
 * It interacts with the {@link westwood222.cloud_alloc.repository.ResourceRepository}
 * and {@link StorageManager} to upload, retrieve, and delete
 * resource to all {@link westwood222.cloud_alloc.model.Account} provided by the user
 */
public interface ResourceService {

    /**
     * Search for all resources (known to the system, i.e., uploaded to the cloud through the application) that matches
     * the request's {@link westwood222.cloud_alloc.model.ResourceProperty}. Additionally, response is paged using
     * request's {@link org.springframework.data.domain.Pageable}
     *
     * @param request resource property to perform the search
     * @return List of metadata about the found resources.
     * Note that ResourceLink will null, to get viewLink, use {@link ResourceService#read(ResourceReadRequest)}
     */
    ResourceSearchResponse search(ResourceSearchRequest request);

    /**
     * Upload the Multipart file in the request to the cloud. The destination cloud account will be depended on the
     * implementation. Caller will receive back an id that uniquely identify the resource within the system. Using this
     * id, the caller can later retrieve the file back from whatever cloud account that the resource is stored in.
     *
     * @param request file to upload
     * @return information regarding the uploaded file, and how to retrieve the file back
     * @throws ExternalException when there is an unexpected error during uploading to the cloud
     */
    ResourceUploadResponse upload(ResourceUploadRequest request) throws ExternalException;

    /**
     * Retrieve the file from the cloud provider. The actual cloud account that holds this file will be calculated, and
     * a link to view the file in the cloud will be generated (by the cloud provider). Using the link, user can download
     * the file if needed.
     *
     * @param request information about the file so that the account holds it will be found-able
     * @return metadata about the file as well as the view link
     * @throws ExternalException when there is an unexpected error during uploading to the cloud
     */
    ResourceReadResponse read(ResourceReadRequest request) throws ExternalException;

    /**
     * Delete the file from the cloud provider. The default behavior (isHardDelete = false) is to move the file to
     * trash, and then the file will be removed within 30 days if it is not moved out. If isHardDelete, the file will
     * be deleted immediately.
     *
     * @param request target file identifier and isHardDelete
     * @return the date that the file will be removed permanently (isHardDelete = false)
     * or nothing (isHardDelete = true)
     * @throws ExternalException when there is an unexpected error during uploading to the cloud
     */
    ResourceDeleteResponse delete(ResourceDeleteRequest request) throws ExternalException;
}
