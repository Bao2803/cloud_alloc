package westwood222.cloud_alloc.service.storage.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import westwood222.cloud_alloc.dto.noti.JobCompleteEvent;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteRequest;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteResponse;
import westwood222.cloud_alloc.dto.storage.read.StorageReadRequest;
import westwood222.cloud_alloc.dto.storage.read.StorageReadResponse;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadRequest;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadResponse;
import westwood222.cloud_alloc.mapper.StorageMapper;
import westwood222.cloud_alloc.model.Job;
import westwood222.cloud_alloc.repository.StorageWorkerRepository;
import westwood222.cloud_alloc.service.storage.worker.StorageWorker;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageManagerImpl implements StorageManager {
    private final StorageMapper storageMapper;
    private final StorageWorkerRepository serviceRepository;
    private final KafkaTemplate<String, JobCompleteEvent> kafkaTemplate;

    @Value("${spring.application.noti.user-email}")
    private String userEmail;                           // TODO: allow multi users and send email to the user's email

    @Override
    public StorageUploadResponse upload(StorageUploadRequest request) {
        MultipartFile file = request.getFile();
        StorageWorker storageService = serviceRepository.getServiceBySpace(file.getSize());
        try {
            StorageUploadRequest uploadRequest = storageMapper.toStorageUploadRequest(file);
            StorageUploadResponse uploadResponse = storageService.upload(uploadRequest);

            // TODO: clean this shit up
            JobCompleteEvent jobCompleteEvent = new JobCompleteEvent();
            Job job = new Job();
            job.setOperation(
                    new Job.Operation(
                            Job.Operation.Action.CREATE,
                            uploadResponse.getProvider().name() + " " + uploadResponse.getUsername(),
                            uploadResponse.getName()
                    )
            );
            job.setStatus(true);
            jobCompleteEvent.setJobs(List.of(job));
            jobCompleteEvent.setResourceOwnerEmail(userEmail);
            kafkaTemplate.send(
                    "job_complete",
                    jobCompleteEvent
            );
            // TODO: clean this shit up

            return uploadResponse;
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