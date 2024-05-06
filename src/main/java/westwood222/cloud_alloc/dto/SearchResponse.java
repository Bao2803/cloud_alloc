package westwood222.cloud_alloc.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponse {
    List<String> ids;
    String nextPageToken;
}
