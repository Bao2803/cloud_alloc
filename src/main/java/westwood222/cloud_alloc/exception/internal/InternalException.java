package westwood222.cloud_alloc.exception.internal;

import westwood222.cloud_alloc.exception.BaseException;

public class InternalException extends BaseException {
    public InternalException(String message) {
        super(message);
    }

    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
