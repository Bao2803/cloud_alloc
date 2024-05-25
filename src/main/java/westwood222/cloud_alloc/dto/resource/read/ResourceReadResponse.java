package westwood222.cloud_alloc.dto.resource.read;

import lombok.Data;

import java.util.UUID;

@Data
public class ResourceReadResponse {
    private UUID resourceId;
    private String resourceLink;
    private String resourceName;
    private String resourceMineType;
}
