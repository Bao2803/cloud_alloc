package westwood222.cloud_alloc.dto.storage.manager.upload;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ManagerUploadRequest {
    private MultipartFile[] files;
}
