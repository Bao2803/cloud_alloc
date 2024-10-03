package westwood222.cloud_alloc.dto;

import westwood222.cloud_alloc.exception.handler.ExceptionDTO;

/**
 * DTO for response to API caller.
 * If an API call successes, data will contain the request data and error is NULL.
 * If an API call fails, data will be NULL, and error will contain the error.
 *
 * @param <T> any type that needed to be returned to the client.
 */
public record ResponseDTO<T>(T data, ExceptionDTO error) {
    public static <T> ResponseDTO<T> success(T data) {
        return new ResponseDTO<>(data, null);
    }

    public static <T> ResponseDTO<T> error(ExceptionDTO error) {
        return new ResponseDTO<>(null, error);
    }
}
