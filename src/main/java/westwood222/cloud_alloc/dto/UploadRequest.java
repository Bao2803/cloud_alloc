package westwood222.cloud_alloc.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UploadRequest {
    String fileType;
    String filePath;
    String fileName;
}
