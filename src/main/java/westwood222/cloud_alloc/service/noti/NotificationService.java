package westwood222.cloud_alloc.service.noti;

import westwood222.cloud_alloc.model.Job;

import java.util.List;

public interface NotificationService {
    void notifyOwnerAboutJob(
            String to,
            List<Job> jobs
    );
}
