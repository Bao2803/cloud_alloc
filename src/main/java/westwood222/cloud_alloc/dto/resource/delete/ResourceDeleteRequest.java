package westwood222.cloud_alloc.dto.resource.delete;

import lombok.Data;

import java.util.UUID;

@Data
public class ResourceDeleteRequest {
    private UUID localId;
    private boolean isHardDelete;
}
