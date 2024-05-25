package westwood222.cloud_alloc.exception.handler;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import westwood222.cloud_alloc.exception.BaseException;
import westwood222.cloud_alloc.exception.InsufficientStorage;
import westwood222.cloud_alloc.exception.ResourceNotFound;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {
    public static final String DEFAULT_RECOVER_LINK = "/home";
    private static final String ERROR_CODE_INTERNAL = "INTERNAL_ERROR";
    private static final Map<Class<? extends RuntimeException>, HttpStatus> EXCEPTION_TO_HTTP_STATUS_CODE = Map.of(
            ResourceNotFound.class, HttpStatus.NOT_FOUND,
            InsufficientStorage.class, HttpStatus.INSUFFICIENT_STORAGE
    );
    private static final Map<Class<? extends RuntimeException>, String> EXCEPTION_TO_ERROR_CODE = Map.of(
            ResourceNotFound.class, "Not found",
            InsufficientStorage.class, "Insufficient storage"
    );

    @ExceptionHandler({GoogleJsonResponseException.class})
    ExceptionResponse handleGoogleException(GoogleJsonResponseException exception) {
        log.warn("Google API", exception);

        return ExceptionResponse.builder()
                .status(HttpStatus.resolve(exception.getStatusCode()))
                .errorCode(exception.getStatusMessage())
                .message(exception.getMessage())
                .recoverLink(DEFAULT_RECOVER_LINK)
                .build();
    }

    @ExceptionHandler({Exception.class})
    ExceptionResponse handleInternalException(BaseException exception) {
        log.warn("Internal Error", exception);

        HttpStatus httpStatus = EXCEPTION_TO_HTTP_STATUS_CODE.getOrDefault(
                exception.getClass(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
        String errorCode = EXCEPTION_TO_ERROR_CODE.getOrDefault(exception.getClass(), ERROR_CODE_INTERNAL);
        return ExceptionResponse.builder()
                .status(httpStatus)
                .errorCode(errorCode)
                .message(exception.getMessage())
                .recoverLink(exception.getRecoverLink())
                .build();
    }
}
