package westwood222.cloud_alloc.dto.storage.manager.read;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagerReadResponse {
    private String resourceLink;
    private String resourceName;
    private String resourceMimeType;
}
