package westwood222.cloud_alloc.dto.storage.manager.upload;

import lombok.Builder;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

@Value
@Builder
public class ManagerUploadRequest {
    MultipartFile[] files;
}
