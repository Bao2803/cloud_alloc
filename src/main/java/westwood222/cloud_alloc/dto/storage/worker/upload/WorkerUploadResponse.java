package westwood222.cloud_alloc.dto.storage.worker.upload;

import lombok.Data;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.Provider;

@Data
public class WorkerUploadResponse {
    private String name;
    private String username;
    private String mimeType;
    private String foreignId;
    private Provider provider;
    private Account account;
}
