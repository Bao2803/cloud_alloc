package westwood222.cloud_alloc.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import westwood222.cloud_alloc.dto.noti.JobCompleteEvent;
import westwood222.cloud_alloc.service.noti.NotificationService;

@Component
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    //    @KafkaListener(id = "noti_group", topics = "job_complete")
    void jobCompleteListener(
            @Valid JobCompleteEvent jobCompleteEvent
    ) {
        notificationService.notifyOwnerAboutJob(
                jobCompleteEvent.getResourceOwnerEmail(),
                jobCompleteEvent.getJobs()
        );
    }
}
