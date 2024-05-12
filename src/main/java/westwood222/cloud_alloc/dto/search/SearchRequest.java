package westwood222.cloud_alloc.dto.search;

import lombok.Builder;
import lombok.Data;
import westwood222.cloud_alloc.model.ResourceProperty;

@Data
@Builder
public class SearchRequest {
    private int size;
    private int page;
    private ResourceProperty resourceProperty;
}
