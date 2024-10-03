package westwood222.cloud_alloc.exception.internal;

import java.util.UUID;

public class ResourceNotFound extends InternalException {
    private static final String code = "1";

    public ResourceNotFound(UUID resourceId) {
        super(code, "No resource with id " + resourceId);
    }
}
