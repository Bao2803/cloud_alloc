package westwood222.cloud_alloc.dto.storage.worker.upload;

import lombok.Builder;
import lombok.Value;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.Provider;

@Value
@Builder
public class WorkerUploadResponse {
    String name;
    String username;
    String mimeType;
    String foreignId;
    Provider provider;
    Account account;
}
