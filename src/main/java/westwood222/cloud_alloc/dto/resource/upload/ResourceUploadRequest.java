package westwood222.cloud_alloc.dto.storage.upload;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ResourceUploadRequest {
    private MultipartFile multipartFile;
}
