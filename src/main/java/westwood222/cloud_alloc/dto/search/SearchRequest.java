package westwood222.cloud_alloc.dto.search;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SearchRequest {
    int size;
    String page;
    Map<String, String> conditions;
}
