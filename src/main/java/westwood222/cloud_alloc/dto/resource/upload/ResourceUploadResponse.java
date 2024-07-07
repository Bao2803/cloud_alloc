package westwood222.cloud_alloc.dto.resource.upload;

import lombok.Data;
import westwood222.cloud_alloc.model.Provider;

import java.util.List;

@Data
public class ResourceUploadResponse {
    private List<File> files;

    @Data
    public static final class File {
        private String username;
        private Provider provider;
        private String resourceId;
    }
}
