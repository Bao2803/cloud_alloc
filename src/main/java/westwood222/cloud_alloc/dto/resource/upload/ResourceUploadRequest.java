package westwood222.cloud_alloc.dto.resource.upload;

import lombok.Builder;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

@Value
@Builder
public class ResourceUploadRequest {
    MultipartFile[] files;
}
