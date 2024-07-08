package westwood222.cloud_alloc.dto.storage.manager.upload;

import lombok.Data;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadResponse;

import java.util.List;

@Data
public class ManagerUploadResponse {
    private List<WorkerUploadResponse> files;
}
