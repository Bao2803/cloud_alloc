package westwood222.cloud_alloc.dto.resource.search;

import lombok.Builder;
import lombok.Value;
import westwood222.cloud_alloc.dto.resource.read.ResourceReadResponse;

import java.util.List;

@Value
@Builder
public class ResourceSearchResponse {
    Integer nextPage;
    Integer nextSize;
    Integer totalPage;
    Long totalElements;
    List<ResourceReadResponse> resources;
}
