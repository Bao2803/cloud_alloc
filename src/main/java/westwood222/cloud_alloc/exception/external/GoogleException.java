package westwood222.cloud_alloc.exception.external;

import westwood222.cloud_alloc.model.Provider;

public class GoogleException extends ExternalException {
    public GoogleException(Throwable throwable) {
        super(Provider.google, throwable);
    }
}
