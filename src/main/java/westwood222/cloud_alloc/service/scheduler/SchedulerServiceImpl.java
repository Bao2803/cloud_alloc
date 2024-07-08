package westwood222.cloud_alloc.service.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import westwood222.cloud_alloc.dto.schedule.CreateTaskRequest;
import westwood222.cloud_alloc.dto.schedule.CreateTaskResponse;
import westwood222.cloud_alloc.dto.schedule.DeleteTaskRequest;
import westwood222.cloud_alloc.dto.schedule.DeleteTaskResponse;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class SchedulerServiceImpl implements SchedulerService {
    private final Map<UUID, ScheduledFuture<?>> scheduledTasks;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    public SchedulerServiceImpl(
            ThreadPoolTaskScheduler threadPoolTaskScheduler
    ) {
        this.scheduledTasks = new ConcurrentHashMap<>();
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }

    public CreateTaskResponse schedule(
            CreateTaskRequest request
    ) {
        UUID taskId = UUID.randomUUID();
        ScheduledFuture<?> task = threadPoolTaskScheduler.schedule(request.getTask(), request.getDesiredTime());
        scheduledTasks.put(taskId, task);

        // Also remove the task form the map at desired time
        threadPoolTaskScheduler.schedule(
                () -> scheduledTasks.remove(taskId),
                request.getDesiredTime()
        );
        return new CreateTaskResponse(taskId);
    }

    public DeleteTaskResponse cancel(
            DeleteTaskRequest request
    ) {
        scheduledTasks.get(request.getTaskId()).cancel(false);
        scheduledTasks.remove(request.getTaskId());
        return new DeleteTaskResponse();
    }
}
