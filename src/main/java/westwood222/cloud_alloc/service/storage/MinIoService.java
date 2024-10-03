package westwood222.cloud_alloc.service.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.annotation.Nonnull;
import westwood222.cloud_alloc.dto.storage.worker.delete.WorkerDeleteRequest;
import westwood222.cloud_alloc.dto.storage.worker.delete.WorkerDeleteResponse;
import westwood222.cloud_alloc.dto.storage.worker.read.WorkerReadRequest;
import westwood222.cloud_alloc.dto.storage.worker.read.WorkerReadResponse;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadRequest;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadResponse;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.service.storage.worker.StorageWorker;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MinIoService extends StorageWorker {
    private final MinioClient minioClient;

    public MinIoService(@Nonnull Account account, MinioClient minioClient) {
        super(account);
        this.minioClient = minioClient;
    }

    @Override
    public long getFreeSpace() {
        return -1;
    }

    @Override
    public WorkerUploadResponse upload(WorkerUploadRequest request) {
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("response-content-type", "application/json");

        try {
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket("test")
                    .object(request.getFile().getOriginalFilename())
                    .expiry(1, TimeUnit.DAYS)
                    .extraQueryParams(reqParams)
                    .build());
            WorkerUploadResponse response = new WorkerUploadResponse();
            response.setForeignId(url);
            return response;
        } catch (
                ServerException
                | InsufficientDataException
                | ErrorResponseException
                | IOException
                | NoSuchAlgorithmException
                | InvalidKeyException
                | InvalidResponseException
                | XmlParserException
                | InternalException e
        ) {
            throw new RuntimeException("Error generating MinIO url for upload", e);
        }
    }

    @Override
    public WorkerReadResponse read(WorkerReadRequest request) {
        WorkerReadResponse response = new WorkerReadResponse();
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket("test")
                            .object(request.getForeignId())
                            .expiry(2, TimeUnit.HOURS)
                            .build());
            response.setResourceLink(url);
            return response;
        } catch (
                ServerException
                | InsufficientDataException
                | ErrorResponseException
                | IOException
                | NoSuchAlgorithmException
                | InvalidKeyException
                | InvalidResponseException
                | XmlParserException
                | InternalException e
        ) {
            throw new RuntimeException("Error generating MinIO url for download", e);
        }
    }

    @Override
    public WorkerDeleteResponse delete(WorkerDeleteRequest request) {
        return null;
    }
}
