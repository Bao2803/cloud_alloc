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

/**
 * This service handle the resources (files and folders).
 * It supports uploading, reading, searching, and deleting resource on user's cloud storage account.
 */
public interface ResourceService {

    /**
     * Search for all resources known to the system (uploaded through the application) that matches the request.
     *
     * @param request resource property to perform the search
     * @return List of metadata about the found resources.
     * @apiNote ResourceLink will null, to get viewLink, use {@link #read(ResourceReadRequest)}
     */
    ResourceSearchResponse search(ResourceSearchRequest request);

    /**
     * Upload the files to the cloud.
     * The destination cloud account will be depended on the implementation.
     * Caller will receive back the files' IDs, which can be used to interact with the files.
     *
     * @param request files to upload
     * @return uploaded files' ids
     * @throws ExternalException when there is an unexpected error during uploading to the cloud
     */
    ResourceUploadResponse upload(ResourceUploadRequest request) throws ExternalException;

    /**
     * Retrieve a link to view the file in the cloud will be generated using the request.
     * User can download the file if needed using the provided link.
     *
     * @param request information about the file so that the account holds it can be found
     * @return file's metadata and its view link
     * @throws ExternalException when there is an unexpected error during uploading to the cloud
     */
    ResourceReadResponse read(ResourceReadRequest request) throws ExternalException;

    /**
     * Delete the file from the cloud provider. The default behavior (isHardDelete = false) is to move the file to
     * trash, and will be removed within a period of time (depend on implementation) if it is not moved out.
     * If isHardDelete, the file will be deleted immediately.
     *
     * @param request file's ID and isHardDelete param
     * @return the date that the file will be removed permanently (isHardDelete = false) or null (isHardDelete = true)
     * @throws ExternalException when there is an unexpected error during uploading to the cloud
     */
    ResourceDeleteResponse delete(ResourceDeleteRequest request) throws ExternalException;
}
