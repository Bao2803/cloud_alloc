package westwood222.cloud_alloc.dto.storage.worker.read;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WorkerReadResponse {
    String resourceLink;
    String resourceName;
    String resourceMimeType;
}
