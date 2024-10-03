package westwood222.cloud_alloc.dto.resource.read;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ResourceReadRequest {
    UUID resourceId;
}
