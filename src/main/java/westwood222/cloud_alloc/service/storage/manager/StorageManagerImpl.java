package westwood222.cloud_alloc.service.storage.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import westwood222.cloud_alloc.dto.noti.JobCompleteEvent;
import westwood222.cloud_alloc.dto.storage.manager.delete.ManagerDeleteRequest;
import westwood222.cloud_alloc.dto.storage.manager.delete.ManagerDeleteResponse;
import westwood222.cloud_alloc.dto.storage.manager.read.ManagerReadRequest;
import westwood222.cloud_alloc.dto.storage.manager.read.ManagerReadResponse;
import westwood222.cloud_alloc.dto.storage.manager.upload.ManagerUploadRequest;
import westwood222.cloud_alloc.dto.storage.manager.upload.ManagerUploadResponse;
import westwood222.cloud_alloc.dto.storage.worker.delete.WorkerDeleteRequest;
import westwood222.cloud_alloc.dto.storage.worker.delete.WorkerDeleteResponse;
import westwood222.cloud_alloc.dto.storage.worker.read.WorkerReadRequest;
import westwood222.cloud_alloc.dto.storage.worker.read.WorkerReadResponse;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadRequest;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadResponse;
import westwood222.cloud_alloc.repository.StorageWorkerRepository;
import westwood222.cloud_alloc.service.storage.worker.StorageWorker;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageManagerImpl implements StorageManager {
    private final StorageWorkerRepository serviceRepository;
    private final KafkaTemplate<String, JobCompleteEvent> kafkaTemplate;

    @Value("${spring.application.noti.user-email}")
    private String userEmail;                           // TODO: allow multi users and send email to the user's email

    @Override
    public ManagerUploadResponse upload(ManagerUploadRequest request) {
        MultipartFile[] files = request.getFiles();
        ManagerUploadResponse response = new ManagerUploadResponse();
        response.setFiles(new ArrayList<>());
        for (MultipartFile file : files) {
            StorageWorker storageService = serviceRepository.getServiceBySpace(file.getSize());
            try {
                WorkerUploadRequest workerRequest = WorkerUploadRequest.builder().file(file).build();
                WorkerUploadResponse workerResponse = storageService.upload(workerRequest);
                response.getFiles().add(workerResponse);
            } catch (Exception e) {
                log.error("Fail to upload file {}", file.getOriginalFilename());
            } finally {
                serviceRepository.addService(storageService);
            }
        }
        return response;
    }

    @Override
    public ManagerReadResponse read(ManagerReadRequest request) {
        StorageWorker service = serviceRepository.getServiceById(request.getAccountId());
        try {
            WorkerReadRequest workerRequest = WorkerReadRequest.builder()
                    .foreignId(request.getForeignId())
                    .build();
            WorkerReadResponse workerResponse = service.read(workerRequest);
            return ManagerReadResponse.builder()
                    .resourceMimeType(workerResponse.getResourceMimeType())
                    .resourceName(workerResponse.getResourceName())
                    .resourceLink(workerResponse.getResourceLink())
                    .build();
        } finally {
            serviceRepository.addService(service);
        }
    }

    @Override
    public ManagerDeleteResponse delete(ManagerDeleteRequest request) {
        StorageWorker service = serviceRepository.getServiceById(request.getAccountId());
        try {
            WorkerDeleteRequest workerRequest = WorkerDeleteRequest.builder()
                    .foreignId(request.getForeignId())
                    .isHardDelete(request.isHardDelete())
                    .build();
            WorkerDeleteResponse workerResponse = service.delete(workerRequest);

            return ManagerDeleteResponse.builder()
                    .deleteDate(workerResponse.getDeleteDate())
                    .build();
        } finally {
            serviceRepository.addService(service);
        }
    }
}