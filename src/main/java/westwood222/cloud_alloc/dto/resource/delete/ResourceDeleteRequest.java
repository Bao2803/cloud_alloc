package westwood222.cloud_alloc.dto.resource.delete;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ResourceDeleteRequest {
    UUID localId;
    boolean isHardDelete;
}
