package westwood222.cloud_alloc.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DeleteTaskRequest {
    private UUID taskId;
}
