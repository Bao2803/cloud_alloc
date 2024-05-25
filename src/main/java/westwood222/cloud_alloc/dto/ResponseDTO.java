package westwood222.cloud_alloc.dto;

import lombok.Builder;
import lombok.Data;
import westwood222.cloud_alloc.exception.handler.ExceptionDTO;

/**
 * DTO for response to API caller.
 * If an API call successes, data will contain the request data and error is NULL.
 * If an API call fails, data will be NULL, and error will contain the error.
 *
 * @param <T> any type that needed to be returned to the client.
 */
@Data
@Builder
public class ResponseDTO<T> {
    private T data;
    private ExceptionDTO error;
}
