package westwood222.cloud_alloc.dto.storage.manager.delete;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ManagerDeleteResponse {
    private LocalDate deleteDate;   // null if request is hard delete
}
