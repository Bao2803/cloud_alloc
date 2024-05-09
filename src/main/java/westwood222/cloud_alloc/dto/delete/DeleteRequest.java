package westwood222.cloud_alloc.dto.delete;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteRequest {
    private String id;
    private boolean isHardDelete;
}
