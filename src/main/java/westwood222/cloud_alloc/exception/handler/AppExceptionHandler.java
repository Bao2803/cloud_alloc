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
import westwood222.cloud_alloc.exception.internal.InsufficientStorage;
import westwood222.cloud_alloc.exception.internal.ResourceNotFound;

import java.util.Map;
import java.util.Set;

@Slf4j
@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Map<Class<? extends RuntimeException>, HttpStatus> EXCEPTION_TO_HTTP_STATUS_CODE = Map.of(
            ResourceNotFound.class, HttpStatus.NOT_FOUND,
            InsufficientStorage.class, HttpStatus.INSUFFICIENT_STORAGE,
            ExternalException.class, HttpStatus.INTERNAL_SERVER_ERROR
    );

    private static final Set<Class<? extends RuntimeException>> SENSITIVE_ERROR = Set.of(
            ExternalException.class
    );

    private ExceptionDTO buildExceptionDTO(BaseException exception) {
        final String GENERAL_MESSAGE = "Something went wrong, please contact us if the problem persist";

        ExceptionDTO dto = new ExceptionDTO();
        if (SENSITIVE_ERROR.contains(exception.getClass())) {
            dto.setError(null);
            dto.setMessage(GENERAL_MESSAGE);
        }
        dto.setError(exception);
        dto.setMessage(exception.getMessage());

        return dto;
    }

    @ExceptionHandler({Exception.class})
    ResponseEntity<ResponseDTO<ExceptionDTO>> handleExceptions(BaseException exception) {
        log.error(String.valueOf(exception));
        exception.printStackTrace(System.err);

        HttpStatus httpStatus = EXCEPTION_TO_HTTP_STATUS_CODE.getOrDefault(
                exception.getClass(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        ResponseDTO<ExceptionDTO> responseDTO = ResponseDTO.<ExceptionDTO>builder()
                .error(buildExceptionDTO(exception))
                .build();

        return ResponseEntity
                .status(httpStatus)
                .body(responseDTO);
    }
}
