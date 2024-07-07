package westwood222.cloud_alloc.service.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import westwood222.cloud_alloc.dto.resource.delete.ResourceDeleteRequest;
import westwood222.cloud_alloc.dto.resource.delete.ResourceDeleteResponse;
import westwood222.cloud_alloc.dto.resource.read.ResourceReadRequest;
import westwood222.cloud_alloc.dto.resource.read.ResourceReadResponse;
import westwood222.cloud_alloc.dto.resource.search.ResourceSearchRequest;
import westwood222.cloud_alloc.dto.resource.search.ResourceSearchResponse;
import westwood222.cloud_alloc.dto.resource.upload.ResourceUploadRequest;
import westwood222.cloud_alloc.dto.resource.upload.ResourceUploadResponse;
import westwood222.cloud_alloc.dto.storage.manager.delete.ManagerDeleteRequest;
import westwood222.cloud_alloc.dto.storage.manager.delete.ManagerDeleteResponse;
import westwood222.cloud_alloc.dto.storage.manager.read.ManagerReadRequest;
import westwood222.cloud_alloc.dto.storage.manager.read.ManagerReadResponse;
import westwood222.cloud_alloc.dto.storage.manager.upload.ManagerUploadRequest;
import westwood222.cloud_alloc.dto.storage.manager.upload.ManagerUploadResponse;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadResponse;
import westwood222.cloud_alloc.exception.internal.ResourceNotFound;
import westwood222.cloud_alloc.mapper.ResourceMapper;
import westwood222.cloud_alloc.mapper.StorageMapper;
import westwood222.cloud_alloc.model.Resource;
import westwood222.cloud_alloc.model.ResourceProperty;
import westwood222.cloud_alloc.repository.ResourceRepository;
import westwood222.cloud_alloc.service.storage.manager.StorageManager;

import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
        Page<Resource> page = resourceRepository.findAllByProperty_NameLikeOrProperty_MimeTypeLike(
                "%" + request.getResourceProperty().getName() + "%",
                "%" + request.getResourceProperty().getMimeType() + "%",
                request.getPageable()
        );

        // Calculate next page and transform data to DTO
        int totalPage = page.getTotalPages();
        int nextSize = page.getSize();
        int nextPage = totalPage == 0 ? 0 : (request.getPageable().getPageNumber() + 1) % totalPage;
        List<ResourceReadResponse> resources = page.getContent()
                .stream().map(resourceMapper::resourceToResourceReadResponse).toList();

        return resourceMapper.toSearchResponse(nextPage, nextSize, totalPage, resources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceUploadResponse upload(ResourceUploadRequest request) {
        // Upload to Cloud
        ManagerUploadRequest storageRequest = storageMapper.toStorageUploadRequest(request.getFiles());
        ManagerUploadResponse storageResponse = storageManager.upload(storageRequest);

        // Save metadata to DB
        ResourceUploadResponse response = new ResourceUploadResponse();
        response.setFiles(new ArrayList<>());
        for (WorkerUploadResponse file : storageResponse.getFiles()) {
            ResourceProperty property = ResourceProperty.builder()
                    .name(file.getName())
                    .mimeType(file.getMimeType())
                    .build();
            Resource resource = Resource.builder()
                    .account(file.getAccount())
                    .foreignId(file.getForeignId())
                    .property(property)
                    .build();
            resource = resourceRepository.save(resource);
            response.getFiles().add(resourceMapper.toResourceUploadResponse(resource.getId(), file));
        }

        return response;
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
        ManagerReadRequest storageRequest = storageMapper.toStorageReadRequest(
                resource.getAccount().getId(),
                resource.getForeignId()
        );
        ManagerReadResponse storageResponse = storageManager.read(storageRequest);

        return resourceMapper.toResourceReadResponse(resource, storageResponse.getResourceLink());
    }

    @Scheduled(cron = "${spring.application.core.fragmentation-cron}")
    public void defragmentation() {
        Instant now = Instant.now();
        List<Resource> resources = resourceRepository.findAllByUpdatedAtBetween(
                now.minus(1, ChronoUnit.DAYS),
                now
        );
        resources.sort((a, b) -> Math.toIntExact(a.getProperty().getSize() - b.getProperty().getSize()));

        // Get the list of object from MinIO.
        // Assuming that MinIO is keeping the most recent state of the new files as well as the update files
        List<InputStream> files = List.of();
        for (InputStream stream : files) {
            // upload file using manager
        }
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
        ManagerDeleteRequest storageRequest = storageMapper.toStorageDeleteRequest(
                resource.getAccount().getId(),
                resource.getForeignId(),
                request.isHardDelete()
        );
        ManagerDeleteResponse storageResponse = storageManager.delete(storageRequest);

        // Delete resource metadata
        resourceRepository.deleteById(request.getLocalId());
        if (request.isHardDelete()) {
            // do hard delete somehow
            System.err.println("NOT IMPLEMENTED: Should be hard delete!");
        }

        return resourceMapper.storageDeleteResponsetoResourceDeleteResponse(storageResponse);
    }
}
