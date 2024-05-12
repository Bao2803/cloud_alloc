package westwood222.cloud_alloc.dto.upload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import westwood222.cloud_alloc.model.ResourceProperty;

@Getter
@RequiredArgsConstructor
public class UploadRequest {
    private String resourcePath;    // local
    private ResourceProperty resourceProperty;
    private MultipartFile multipartFile;
}
