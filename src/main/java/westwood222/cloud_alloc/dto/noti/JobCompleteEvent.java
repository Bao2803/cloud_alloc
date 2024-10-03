package westwood222.cloud_alloc.dto.noti;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import westwood222.cloud_alloc.model.Job;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class JobCompleteEvent implements Serializable {
    @NotNull(message = "Job can't be null")
    private List<Job> jobs;

    @NotEmpty(message = "Owner's email can't be null")
    private String resourceOwnerEmail;
}
