package westwood222.cloud_alloc.dto.storage.manager.read;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ManagerReadRequest {
    UUID accountId;
    String foreignId;
}
