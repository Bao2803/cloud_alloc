package westwood222.cloud_alloc.dto.storage.delete;

import lombok.Data;

import java.util.UUID;

@Data
public class StorageDeleteRequest {
    private UUID accountId;
    private String foreignId;
    private boolean isHardDelete;
}
