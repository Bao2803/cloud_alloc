package westwood222.cloud_alloc.exception.external;

import westwood222.cloud_alloc.exception.BaseException;
import westwood222.cloud_alloc.model.Provider;

public abstract class ExternalException extends BaseException {
    private static final String prefixCode = "EXT_";

    public ExternalException(Provider provider, Throwable throwable) {
        super(prefixCode + provider.name(), "Error calling " + provider.name() + "'s APIs", throwable);
    }
}
