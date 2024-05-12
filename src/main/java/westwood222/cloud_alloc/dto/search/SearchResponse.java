package westwood222.cloud_alloc.dto.search;

import lombok.Builder;
import lombok.Data;
import westwood222.cloud_alloc.model.Resource;

import java.util.List;

@Data
@Builder
public class SearchResponse {
    int nextPage;
    int nextSize;
    List<Resource> resources;
}
