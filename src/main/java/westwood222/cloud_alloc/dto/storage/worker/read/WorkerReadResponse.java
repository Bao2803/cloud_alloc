package westwood222.cloud_alloc.dto.storage.worker.read;

import lombok.Data;

@Data
public class WorkerReadResponse {
    private String resourceLink;
    private String resourceName;
    private String resourceMimeType;
}
