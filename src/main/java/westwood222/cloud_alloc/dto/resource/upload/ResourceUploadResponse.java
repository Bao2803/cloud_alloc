package westwood222.cloud_alloc.dto.resource.upload;

import lombok.Data;
import westwood222.cloud_alloc.model.Provider;

@Data
public class ResourceUploadResponse {
    private String username;
    private Provider provider;
    private String resourceId;
}
