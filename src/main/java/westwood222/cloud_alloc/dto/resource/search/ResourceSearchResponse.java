package westwood222.cloud_alloc.dto.storage.search;

import lombok.Data;
import westwood222.cloud_alloc.model.Resource;

import java.util.List;

@Data
public class ResourceSearchResponse {
    int nextPage;
    int totalSize;
    List<Resource> resources;
}
