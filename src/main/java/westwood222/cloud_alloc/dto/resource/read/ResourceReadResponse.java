package westwood222.cloud_alloc.dto.resource.read;

import lombok.Builder;
import lombok.Value;
import westwood222.cloud_alloc.model.Provider;

import java.util.UUID;

@Value
@Builder
public class ResourceReadResponse {
    UUID resourceId;
    String username;
    Provider provider;
    String resourceLink;
    String resourceName;
    String resourceMimeType;
}
