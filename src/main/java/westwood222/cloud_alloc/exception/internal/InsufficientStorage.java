package westwood222.cloud_alloc.exception.internal;

public class InsufficientStorage extends InternalException {
    private static final String code = "2";

    public InsufficientStorage(long byteNeeded) {
        super(code, String.format("Insufficient storage to upload file with %d bytes", byteNeeded));
    }
}
