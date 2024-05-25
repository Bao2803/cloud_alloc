package westwood222.cloud_alloc.exception.external;

import westwood222.cloud_alloc.exception.BaseException;
import westwood222.cloud_alloc.model.Provider;

public abstract class ExternalException extends BaseException {
    public ExternalException(Provider provider, Throwable throwable) {
        super(provider.name(), throwable);
    }
}
