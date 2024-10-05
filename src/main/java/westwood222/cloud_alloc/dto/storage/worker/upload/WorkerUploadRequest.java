package westwood222.cloud_alloc.dto.storage.worker.upload;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class WorkerUploadRequest {
    private String mimeType;
    private MultipartFile file;
}
