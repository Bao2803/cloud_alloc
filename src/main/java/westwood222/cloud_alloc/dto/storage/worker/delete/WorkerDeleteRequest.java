package westwood222.cloud_alloc.dto.storage.worker.delete;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkerDeleteRequest {
    private String foreignId;
    private boolean isHardDelete;
}
