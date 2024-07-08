package westwood222.cloud_alloc.dto.resource.search;

import lombok.Data;
import org.springframework.data.domain.Pageable;
import westwood222.cloud_alloc.model.ResourceProperty;

@Data
public class ResourceSearchRequest {
    private Pageable pageable;
    private ResourceProperty resourceProperty;
}
