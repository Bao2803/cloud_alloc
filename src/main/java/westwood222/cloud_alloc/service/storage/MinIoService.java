package westwood222.cloud_alloc.service.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteRequest;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteResponse;
import westwood222.cloud_alloc.dto.storage.read.StorageReadRequest;
import westwood222.cloud_alloc.dto.storage.read.StorageReadResponse;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadRequest;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadResponse;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinIoService implements StorageService {
    private final MinioClient minioClient;

    @Override
    public StorageUploadResponse upload(StorageUploadRequest request) {
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
            StorageUploadResponse response = new StorageUploadResponse();
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
    public StorageReadResponse read(StorageReadRequest request) {
        StorageReadResponse response = new StorageReadResponse();
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
    public StorageDeleteResponse delete(StorageDeleteRequest request) {
        return null;
    }
}
