package westwood222.cloud_alloc.dto.storage.worker.read;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkerReadRequest {
    private String foreignId;
}
