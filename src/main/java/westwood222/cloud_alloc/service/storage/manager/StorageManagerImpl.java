package westwood222.cloud_alloc.service.storage.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteRequest;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteResponse;
import westwood222.cloud_alloc.dto.storage.read.StorageReadRequest;
import westwood222.cloud_alloc.dto.storage.read.StorageReadResponse;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadRequest;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadResponse;
import westwood222.cloud_alloc.mapper.StorageMapper;
import westwood222.cloud_alloc.repository.StorageWorkerRepository;
import westwood222.cloud_alloc.service.storage.worker.StorageWorker;

@Service
@RequiredArgsConstructor
public class StorageManagerImpl implements StorageManager {

    private final StorageMapper storageMapper;
    private final StorageWorkerRepository serviceRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public StorageUploadResponse upload(StorageUploadRequest request) {
        MultipartFile file = request.getFile();
        StorageWorker storageService = serviceRepository.getServiceBySpace(file.getSize());
        try {
            StorageUploadRequest storageRequest = storageMapper.toStorageUploadRequest(file);
            return storageService.upload(storageRequest);
        } finally {
            serviceRepository.addService(storageService);
        }
    }

    @Override
    public StorageReadResponse read(StorageReadRequest request) {
        StorageWorker service = serviceRepository.getServiceById(request.getAccountId());
        try {
            StorageReadRequest storageRequest = storageMapper.toStorageReadRequest(
                    null,
                    request.getForeignId()
            );
            return service.read(storageRequest);
        } finally {
            serviceRepository.addService(service);
        }
    }

    @Override
    public StorageDeleteResponse delete(StorageDeleteRequest request) {
        StorageWorker service = serviceRepository.getServiceById(request.getAccountId());
        try {
            StorageDeleteRequest storageRequest = storageMapper.toStorageDeleteRequest(
                    null,
                    request.getForeignId(),
                    request.isHardDelete()
            );

            return service.delete(storageRequest);
        } finally {
            serviceRepository.addService(service);
        }
    }
}