package westwood222.cloud_alloc.dto.resource.upload;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class ResourceUploadResponse {
    List<File> files;

    @Value
    @Builder
    public static class File {
        UUID resourceId;
    }
}
