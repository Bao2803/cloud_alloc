package westwood222.cloud_alloc.service.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import westwood222.cloud_alloc.exception.internal.ResourceNotFound;
import westwood222.cloud_alloc.model.Provider;
import westwood222.cloud_alloc.model.Resource;
import westwood222.cloud_alloc.repository.ResourceRepository;
import westwood222.cloud_alloc.repository.StorageWorkerRepository;
import westwood222.cloud_alloc.service.storage.manager.StorageManager;
import westwood222.cloud_alloc.service.storage.worker.CloudStorageService;

import java.net.FileNameMap;
import java.net.URLConnection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final ResourceRepository resourceRepository;

    private final StorageManager storageManager;

    private final StorageWorkerRepository workerRepository;     // temp for demo purpose; should call through manager
    private final FileNameMap fileNameMap = URLConnection.getFileNameMap();

    @Override
    public ResourceSearchResponse search(ResourceSearchRequest request) {
        // Search
        Page<Resource> page = resourceRepository.findAllByProperty_NameLikeOrProperty_MimeTypeLike(
                "%" + request.getResourceProperty().getName() + "%",
                "%" + request.getResourceProperty().getMimeType() + "%",
                request.getPageable()
        );

        // Calculate next page and transform data to DTO
        Pageable nextPageable = page.nextOrLastPageable();
        int totalPage = page.getTotalPages();
        int nextSize = nextPageable.getPageSize();
        int nextPage = nextPageable.getPageNumber();
        long totalElements = page.getTotalElements();

        // Get all resources
        List<ResourceReadResponse> resources = page.getContent()
                .stream()
                .map(
                        resource -> ResourceReadResponse.builder()
                                .resourceId(resource.getId())
                                .resourceName(resource.getProperty().getName())
                                .resourceMimeType(resource.getProperty().getMimeType())
                                .build()
                )
                .toList();

        return ResourceSearchResponse.builder()
                .nextPage(nextPage)
                .nextSize(nextSize)
                .totalPage(totalPage)
                .totalElements(totalElements)
                .resources(resources)
                .build();
    }

    @Override
    public ResourceUploadResponse upload(ResourceUploadRequest request) {
        return null;
    }

    @Override
    public ResourceReadResponse read(ResourceReadRequest request) {
        // Get resource metadata
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFound(request.getResourceId()));

        ManagerReadRequest managerReadRequest = ManagerReadRequest.builder()
                .accountId(resource.getAccount().getId())
                .foreignId(resource.getForeignId())
                .build();
        ManagerReadResponse storageResponse = storageManager.read(managerReadRequest);

        return ResourceReadResponse.builder()
                .resourceName(storageResponse.getResourceName())
                .resourceMimeType(storageResponse.getResourceMimeType())
                .resourceLink(storageResponse.getResourceLink())
                .build();
    }

    @Scheduled(cron = "${spring.application.core.fragmentation-cron}")
    public void defragmentation() {
        // Get all files that are updated today
        Instant now = Instant.now();
        ArrayDeque<Resource> resources = resourceRepository.findAllByUpdatedAtBetweenOrderByProperty_SizeAsc(
                now.minus(1, ChronoUnit.DAYS),
                now
        );

        // Upload alternatively the largest and smallest file to the account with less space
        // If an InsufficientStorage is thrown,
        // try to upload as much file as possible from smallest to largest
        Resource curr;
        CloudStorageService worker = null;
        boolean isFront = false;
        while (!resources.isEmpty()) {
            // Obtain a resource alternatively between the largest file and smallest file
            // Group them into large/small block to decrease level of fragmentation
            if (isFront) {
                curr = resources.removeFirst();
            } else {
                curr = resources.removeLast();
            }
            isFront = !isFront;

            // Logic to get the actual file content here
            // We will get all files from MinIO,
            // as well as modified files on Google Drive using https://developers.google.com/drive/api/guides/push

            // Move a file to the account with less free space, but still has enough storage to handle this file
            try {
                worker = workerRepository.getServiceBySpace(curr.getProperty().getSize());
                log.info("Move {} to {}", curr.getProperty(), worker.getAccount());

                // decrease the space
                // this will be done automatically by the worker,
                // but we do it here manually for demo purpose
                worker.setFreeSpace(worker.getFreeSpace() - curr.getProperty().getSize());
            } catch (Exception e) {
                // catch exception if any; we don't want to stop the batch if there is an error on 1 file
                // save the file into a queue or sth for retry
                log.error("Error moving {}; continue bath process", curr.getProperty());
                continue;
            } finally {
                if (worker != null) workerRepository.addService(worker);
            }

            // Only remove file if the file successfully moved to the new destination
            try {
                worker = workerRepository.getServiceById(curr.getAccount().getId());
                log.info("Remove file from {}", worker.getAccount());

                // increase the space
                // this will be done automatically by the worker,
                // but we do it here manually for demo purpose
                if (curr.getAccount().getProvider() != Provider.MINIO) {
                    worker.setFreeSpace(worker.getFreeSpace() + curr.getProperty().getSize());
                }
            } catch (Exception e) {
                log.error("Failed to remove file {} from {}", curr.getProperty(), worker.getAccount());
                // move the remove action into a queue for later retry
            } finally {
                workerRepository.addService(worker);
            }
        }
    }

    @Override
    public ResourceDeleteResponse delete(ResourceDeleteRequest request) {
        // Get resource metadata
        Resource resource = resourceRepository.findById(request.getLocalId())
                .orElseThrow(() -> new ResourceNotFound(request.getLocalId()));

        // Delete resource from cloud storage
        ManagerDeleteRequest storageRequest = ManagerDeleteRequest.builder()
                .accountId(resource.getAccount().getId())
                .foreignId(resource.getForeignId())
                .isHardDelete(request.isHardDelete())
                .build();
        ManagerDeleteResponse storageResponse = storageManager.delete(storageRequest);

        // Delete resource metadata
        resourceRepository.deleteById(request.getLocalId());
        if (request.isHardDelete()) {
            // do hard delete somehow
            System.err.println("NOT IMPLEMENTED: Should be hard delete!");
        }

        return ResourceDeleteResponse.builder()
                .deleteDate(storageResponse.getDeleteDate())
                .build();
    }
}
