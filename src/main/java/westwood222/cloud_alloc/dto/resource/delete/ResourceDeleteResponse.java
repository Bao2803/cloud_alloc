package westwood222.cloud_alloc.dto.resource.delete;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class ResourceDeleteResponse {
    LocalDate deleteDate;
}
