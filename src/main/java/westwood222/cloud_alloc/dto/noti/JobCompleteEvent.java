package westwood222.cloud_alloc.dto.noti;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import westwood222.cloud_alloc.model.Job;

import java.util.List;

@Data
public class JobCompleteEvent {
    @NotNull(message = "Job can't be null")
    private List<Job> jobs;

    @NotEmpty(message = "Owner's email can't be null")
    private String resourceOwnerEmail;
}
