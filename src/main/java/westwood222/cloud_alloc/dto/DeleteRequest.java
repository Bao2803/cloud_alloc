package westwood222.cloud_alloc.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteRequest {
    String id;
    boolean isHardDelete;
}
