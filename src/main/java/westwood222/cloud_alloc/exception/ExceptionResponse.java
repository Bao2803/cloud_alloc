package westwood222.cloud_alloc.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class ExceptionResponse {
    private HttpStatus status;
    private String errorCode;
    private String message;
}
