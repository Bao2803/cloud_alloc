package westwood222.cloud_alloc.dto.resource.upload;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ResourceUploadRequest {
    private MultipartFile[] files;
}
