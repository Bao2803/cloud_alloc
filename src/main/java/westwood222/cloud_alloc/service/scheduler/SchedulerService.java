package westwood222.cloud_alloc.service.scheduler;

import westwood222.cloud_alloc.dto.schedule.CreateTaskRequest;
import westwood222.cloud_alloc.dto.schedule.CreateTaskResponse;
import westwood222.cloud_alloc.dto.schedule.DeleteTaskRequest;
import westwood222.cloud_alloc.dto.schedule.DeleteTaskResponse;

public interface SchedulerService {
    CreateTaskResponse schedule(CreateTaskRequest request);

    DeleteTaskResponse cancel(DeleteTaskRequest request);
}
