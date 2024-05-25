package westwood222.cloud_alloc.dto.resource.search;

import lombok.Data;
import westwood222.cloud_alloc.dto.resource.read.ResourceReadResponse;

import java.util.List;

@Data
public class ResourceSearchResponse {
    int nextPage;
    int totalPage;
    List<ResourceReadResponse> resources;
}
