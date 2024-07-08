package westwood222.cloud_alloc.dto.schedule;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Data
@RequiredArgsConstructor
public class CreateTaskRequest {
    private Runnable task;
    private Instant desiredTime;
}
