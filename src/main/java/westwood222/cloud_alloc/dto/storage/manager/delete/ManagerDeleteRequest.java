package westwood222.cloud_alloc.dto.storage.manager.delete;

import lombok.Data;

import java.util.UUID;

@Data
public class ManagerDeleteRequest {
    private UUID accountId;
    private String foreignId;
    private boolean isHardDelete;
}
