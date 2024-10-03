package westwood222.cloud_alloc.dto.storage.manager.read;

import lombok.Data;

import java.util.UUID;

@Data
public class ManagerReadRequest {
    private UUID accountId;
    private String foreignId;
}
