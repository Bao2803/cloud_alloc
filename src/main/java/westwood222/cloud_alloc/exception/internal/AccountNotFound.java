package westwood222.cloud_alloc.exception.internal;

import java.util.UUID;

public class AccountNotFound extends InternalException {
    private static final String code = "3";

    public AccountNotFound(UUID accountId) {
        super(code, "No account with id " + accountId);
    }
}
