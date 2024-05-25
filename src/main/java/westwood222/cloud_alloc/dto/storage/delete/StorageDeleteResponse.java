package westwood222.cloud_alloc.dto.storage.delete;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StorageDeleteResponse {
    private LocalDate deleteDate;   // null if request is hard delete
}
