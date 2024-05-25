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
import westwood222.cloud_alloc.exception.external.ExternalException;
import westwood222.cloud_alloc.exception.internal.ResourceNotFound;
import westwood222.cloud_alloc.mapper.ResourceMapper;
import westwood222.cloud_alloc.mapper.StorageMapper;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.Resource;
import westwood222.cloud_alloc.model.ResourceProperty;
import westwood222.cloud_alloc.repository.ResourceRepository;
import westwood222.cloud_alloc.service.storage.StorageService;
import westwood222.cloud_alloc.service.storageManager.StorageServiceManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final ResourceMapper resourceMapper;
    private final ResourceRepository resourceRepository;

    private final StorageServiceManager storageServiceManager;
    private final StorageMapper storageMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceSearchResponse search(ResourceSearchRequest request) {
        // Search
        Page<Resource> page = resourceRepository.findByProperty(
                request.getResourceProperty(),
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
    public ResourceUploadResponse upload(ResourceUploadRequest request) throws ExternalException {
        MultipartFile file = request.getFile();
        StorageService service = storageServiceManager.getServiceBySpace(file.getSize());
        try {
            StorageUploadRequest storageRequest = storageMapper.toStorageUploadRequest(file);
            StorageUploadResponse storageResponse = service.upload(storageRequest);

            ResourceProperty property = ResourceProperty.builder()
                    .name(file.getName())
                    .mineType(file.getContentType())
                    .build();

            Resource resource = Resource.builder()
                    .account(service.getAccount())
                    .foreignId(storageResponse.getForeignId())
                    .property(property)
                    .build();
            resource = resourceRepository.save(resource);

            return resourceMapper.toResourceUploadResponse(resource.getId(), storageResponse);
        } finally {
            storageServiceManager.add(service);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceReadResponse read(ResourceReadRequest request) throws ExternalException {
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFound("No resource with id " + request.getResourceId()));

        Account account = resource.getAccount();
        StorageService service = storageServiceManager.getServiceById(account.getId());
        try {
            StorageReadRequest storageRequest = storageMapper.toStorageReadRequest(resource.getForeignId());
            StorageReadResponse storageResponse = service.read(storageRequest);

            return resourceMapper.toResourceReadResponse(resource, storageResponse.getResourceLink());
        } finally {
            storageServiceManager.add(service);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceDeleteResponse delete(ResourceDeleteRequest request) throws ExternalException {
        Resource resource = resourceRepository.findById(request.getLocalId())
                .orElseThrow(() -> new ResourceNotFound("No resource with id " + request.getLocalId()));
        Account account = resource.getAccount();

        StorageService service = storageServiceManager.getServiceById(account.getId());
        try {
            StorageDeleteRequest storageRequest = storageMapper.toStorageDeleteRequest(
                    resource.getForeignId(),
                    request.isHardDelete()
            );

            StorageDeleteResponse storageResponse = service.delete(storageRequest);
            resourceRepository.deleteById(request.getLocalId());
            if (request.isHardDelete()) {
                // do hard delete somehow
                System.err.println("NOT IMPLEMENTED: Should be hard delete!");
            }

            return resourceMapper.storageDeleteResponsetoResourceDeleteResponse(storageResponse);
        } finally {
            storageServiceManager.add(service);
        }
    }
}
