package westwood222.cloud_alloc.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String ERROR_CODE_INTERNAL = "INTERNAL_ERROR";
    private static final Map<Class<? extends RuntimeException>, HttpStatus> EXCEPTION_TO_HTTP_STATUS_CODE = Map.of(
        ResourceNotFound.class, HttpStatus.NOT_FOUND
    );
    private static final Map<Class<? extends RuntimeException>, String> EXCEPTION_TO_ERROR_CODE = Map.of(
        ResourceNotFound.class, "Not found"
    );

    @ExceptionHandler({GoogleJsonResponseException.class})
    ExceptionResponse handleGoogleException(GoogleJsonResponseException exception) {
        log.warn("Google API", exception);

        return ExceptionResponse.builder()
                .status(HttpStatus.resolve(exception.getStatusCode()))
                .errorCode(exception.getStatusMessage())
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler({Exception.class})
    ExceptionResponse handleInternalException(Exception exception) {
        log.warn("Internal Error", exception);

        HttpStatus httpStatus = EXCEPTION_TO_HTTP_STATUS_CODE.getOrDefault(
                exception.getClass(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
        String errorCode = EXCEPTION_TO_ERROR_CODE.getOrDefault(exception.getClass(), ERROR_CODE_INTERNAL);
        return ExceptionResponse.builder()
                .status(httpStatus)
                .errorCode(errorCode)
                .build();
    }
}
