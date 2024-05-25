package westwood222.cloud_alloc.dto.storage.delete;

import lombok.Data;

@Data
public class ResourceDeleteRequest {
    private String resourceId;
    private boolean isHardDelete;
}
