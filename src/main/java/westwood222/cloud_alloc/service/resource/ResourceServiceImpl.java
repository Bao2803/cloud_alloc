package westwood222.cloud_alloc.service.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import westwood222.cloud_alloc.dto.resource.delete.ResourceDeleteRequest;
import westwood222.cloud_alloc.dto.resource.delete.ResourceDeleteResponse;
import westwood222.cloud_alloc.dto.resource.read.ResourceReadRequest;
import westwood222.cloud_alloc.dto.resource.read.ResourceReadResponse;
import westwood222.cloud_alloc.dto.resource.search.ResourceSearchRequest;
import westwood222.cloud_alloc.dto.resource.search.ResourceSearchResponse;
import westwood222.cloud_alloc.dto.resource.upload.ResourceUploadRequest;
import westwood222.cloud_alloc.dto.resource.upload.ResourceUploadResponse;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteRequest;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteResponse;
import westwood222.cloud_alloc.dto.storage.read.StorageReadRequest;
import westwood222.cloud_alloc.dto.storage.read.StorageReadResponse;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadRequest;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadResponse;
import westwood222.cloud_alloc.exception.internal.ResourceNotFound;
import westwood222.cloud_alloc.mapper.ResourceMapper;
import westwood222.cloud_alloc.mapper.StorageMapper;
import westwood222.cloud_alloc.model.Resource;
import westwood222.cloud_alloc.model.ResourceProperty;
import westwood222.cloud_alloc.repository.ResourceRepository;
import westwood222.cloud_alloc.service.storage.manager.StorageManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final ResourceMapper resourceMapper;
    private final ResourceRepository resourceRepository;

    private final StorageMapper storageMapper;
    private final StorageManager storageManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceSearchResponse search(ResourceSearchRequest request) {
        // Search
        Page<Resource> page = resourceRepository.findAllByProperty_NameLikeOrProperty_MineTypeLike(
                "%" + request.getResourceProperty().getName() + "%",
                "%" + request.getResourceProperty().getMineType() + "%",
                request.getPageable()
        );

        // Calculate next page and transform data to DTO
        int totalPage = page.getTotalPages();
        int nextPage = totalPage == 0 ? 0 : (request.getPageable().getPageNumber() + 1) % totalPage;
        List<ResourceReadResponse> resources = page.getContent()
                .stream().map(resourceMapper::resourceToResourceReadResponse).toList();

        return resourceMapper.toSearchResponse(nextPage, totalPage, resources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceUploadResponse upload(ResourceUploadRequest request) {
        MultipartFile file = request.getFile();

        // Upload to Cloud
        StorageUploadRequest storageRequest = storageMapper.toStorageUploadRequest(file);
        StorageUploadResponse storageResponse = storageManager.upload(storageRequest);

        // Save metadata to DB
        ResourceProperty property = ResourceProperty.builder()
                .name(file.getName())
                .mineType(file.getContentType())
                .build();
        Resource resource = Resource.builder()
                .account(storageResponse.getAccount())
                .foreignId(storageResponse.getForeignId())
                .property(property)
                .build();
        resource = resourceRepository.save(resource);

        return resourceMapper.toResourceUploadResponse(resource.getId(), storageResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceReadResponse read(ResourceReadRequest request) {
        // Get resource metadata
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFound(request.getResourceId()));

        // Get resource download link from cloud storage
        StorageReadRequest storageRequest = storageMapper.toStorageReadRequest(
                resource.getAccount().getId(),
                resource.getForeignId()
        );
        StorageReadResponse storageResponse = storageManager.read(storageRequest);

        return resourceMapper.toResourceReadResponse(resource, storageResponse.getResourceLink());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceDeleteResponse delete(ResourceDeleteRequest request) {
        // Get resource metadata
        Resource resource = resourceRepository.findById(request.getLocalId())
                .orElseThrow(() -> new ResourceNotFound(request.getLocalId()));

        // Delete resource from cloud storage
        StorageDeleteRequest storageRequest = storageMapper.toStorageDeleteRequest(
                resource.getAccount().getId(),
                resource.getForeignId(),
                request.isHardDelete()
        );
        StorageDeleteResponse storageResponse = storageManager.delete(storageRequest);

        // Delete resource metadata
        resourceRepository.deleteById(request.getLocalId());
        if (request.isHardDelete()) {
            // do hard delete somehow
            System.err.println("NOT IMPLEMENTED: Should be hard delete!");
        }

        return resourceMapper.storageDeleteResponsetoResourceDeleteResponse(storageResponse);
    }
}
