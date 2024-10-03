package westwood222.cloud_alloc.dto.storage.manager.delete;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ManagerDeleteRequest {
    UUID accountId;
    String foreignId;
    boolean isHardDelete;
}
