package westwood222.cloud_alloc.dto.upload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UploadRequest {
    private String fileType;
    private String filePath;
    private String fileName;
}
