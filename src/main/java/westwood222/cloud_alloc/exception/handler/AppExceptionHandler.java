package westwood222.cloud_alloc.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import westwood222.cloud_alloc.dto.ResponseDTO;
import westwood222.cloud_alloc.exception.BaseException;
import westwood222.cloud_alloc.exception.external.ExternalException;
import westwood222.cloud_alloc.exception.internal.AccountNotFound;
import westwood222.cloud_alloc.exception.internal.InsufficientStorage;
import westwood222.cloud_alloc.exception.internal.InternalException;
import westwood222.cloud_alloc.exception.internal.ResourceNotFound;

import java.util.Map;
import java.util.Set;

@Slf4j
@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String GENERAL_CODE = "UNKNOWN";
    private static final String GENERAL_MSG = "Something went wrong, please contact us if the problem persist";

    private static final Map<Class<? extends RuntimeException>, HttpStatus> EXCEPTION_TO_HTTP_STATUS_CODE = Map.of(
            // Internal exceptions
            AccountNotFound.class, HttpStatus.NOT_FOUND,
            ResourceNotFound.class, HttpStatus.NOT_FOUND,
            InsufficientStorage.class, HttpStatus.INSUFFICIENT_STORAGE,

            // External exceptions, let's have all of them be 503
            ExternalException.class, HttpStatus.SERVICE_UNAVAILABLE
    );

    private static final Set<Class<? extends RuntimeException>> WHITE_LIST_ERROR = Set.of(
            AccountNotFound.class,
            ResourceNotFound.class,
            InsufficientStorage.class
    );

    private ResponseEntity<ResponseDTO<ExceptionDTO>> createResponseEntity(BaseException exception) {
        final ExceptionDTO exceptionDTO = new ExceptionDTO();
        exceptionDTO.setCode(exception.getCode());

        String msg = GENERAL_MSG;
        if (WHITE_LIST_ERROR.contains(exception.getClass())) {
            msg = exception.getMessage();
        }
        exceptionDTO.setMessage(msg);

        HttpStatus status = EXCEPTION_TO_HTTP_STATUS_CODE.getOrDefault(
                exception.getClass(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
        ResponseDTO<ExceptionDTO> body = ResponseDTO.error(exceptionDTO);

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler
    ResponseEntity<ResponseDTO<ExceptionDTO>> handleExceptions(Exception unknownError) {
        log.error(String.valueOf(unknownError));
        unknownError.printStackTrace(System.err);

        ExceptionDTO exceptionDTO = new ExceptionDTO();
        exceptionDTO.setCode(GENERAL_CODE);
        exceptionDTO.setMessage(GENERAL_MSG);

        return ResponseEntity.internalServerError().body(ResponseDTO.error(exceptionDTO));
    }

    @ExceptionHandler
    ResponseEntity<ResponseDTO<ExceptionDTO>> handleExceptions(ExternalException externalError) {
        log.error(String.valueOf(externalError));

        return createResponseEntity(externalError);
    }

    @ExceptionHandler
    ResponseEntity<ResponseDTO<ExceptionDTO>> handleExceptions(InternalException internalError) {
        log.warn(String.valueOf(internalError));

        return createResponseEntity(internalError);
    }
}
