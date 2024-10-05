package westwood222.cloud_alloc.dto.resource.read;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceReadResponse {
    UUID resourceId;
    String resourceLink;
    String resourceName;
    String resourceMimeType;
}
