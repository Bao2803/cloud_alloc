package westwood222.cloud_alloc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import westwood222.cloud_alloc.dto.ResponseDTO;
import westwood222.cloud_alloc.dto.schedule.CreateTaskRequest;
import westwood222.cloud_alloc.dto.schedule.CreateTaskResponse;
import westwood222.cloud_alloc.dto.schedule.DeleteTaskRequest;
import westwood222.cloud_alloc.dto.schedule.DeleteTaskResponse;
import westwood222.cloud_alloc.service.scheduler.SchedulerService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedule")
public class SchedulerController {
    private final SchedulerService schedulerService;

    @PostMapping
    public ResponseDTO<CreateTaskResponse> newFutureTask(
            @RequestBody CreateTaskRequest request
    ) {
        CreateTaskResponse response = schedulerService.schedule(request);
        return ResponseDTO.success(response);
    }

    @DeleteMapping("/{taskId}")
    public ResponseDTO<DeleteTaskResponse> removeFutureTask(
            @PathVariable UUID taskId
    ) {
        DeleteTaskResponse response = schedulerService.cancel(new DeleteTaskRequest(taskId));
        return ResponseDTO.success(response);
    }
}
