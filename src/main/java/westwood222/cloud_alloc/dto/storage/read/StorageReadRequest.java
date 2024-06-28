package westwood222.cloud_alloc.dto.storage.read;

import lombok.Data;

import java.util.UUID;

@Data
public class StorageReadRequest {
    private UUID accountId;
    private String foreignId;
}
