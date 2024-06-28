package westwood222.cloud_alloc.dto.storage.upload;

import lombok.Data;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.Provider;

@Data
public class StorageUploadResponse {
    private String name;
    private String username;
    private String mineType;
    private String foreignId;
    private Provider provider;
    private Account account;
}
