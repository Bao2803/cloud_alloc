package westwood222.cloud_alloc.dto.storage.delete;

import lombok.Data;

@Data
public class StorageDeleteRequest {
    private String foreignId;
    private boolean isHardDelete;
}
