package westwood222.cloud_alloc.service.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import westwood222.cloud_alloc.dto.storage.worker.read.WorkerReadRequest;
import westwood222.cloud_alloc.dto.storage.worker.read.WorkerReadResponse;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadRequest;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadResponse;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinIoService {
    private final MinioClient minioClient;

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
}
