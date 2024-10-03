package westwood222.cloud_alloc.exception.internal;

import westwood222.cloud_alloc.exception.BaseException;

public abstract class InternalException extends BaseException {
    private static final String codePrefix = "INT_";

    public InternalException(String code, String message) {
        super(codePrefix + code, message);
    }
}
