package westwood222.cloud_alloc.dto.resource.read;

import lombok.Data;
import westwood222.cloud_alloc.model.Provider;

import java.util.UUID;

@Data
public class ResourceReadResponse {
    private UUID resourceId;
    private String username;
    private Provider provider;
    private String resourceLink;
    private String resourceName;
    private String resourceMimeType;
}
