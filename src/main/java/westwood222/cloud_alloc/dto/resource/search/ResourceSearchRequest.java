package westwood222.cloud_alloc.dto.resource.search;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Pageable;
import westwood222.cloud_alloc.model.ResourceProperty;

@Value
@Builder
public class ResourceSearchRequest {
    Pageable pageable;
    ResourceProperty resourceProperty;
}
