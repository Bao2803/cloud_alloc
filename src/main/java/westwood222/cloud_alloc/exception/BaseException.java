package westwood222.cloud_alloc.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    private final String code;

    public BaseException(String code) {
        this(code, "");
    }

    public BaseException(String code, Throwable cause) {
        this(code, null, cause);
    }

    public BaseException(String code, String message) {
        this(code, message, null);
    }

    public BaseException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
