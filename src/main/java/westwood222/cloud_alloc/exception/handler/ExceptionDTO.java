package westwood222.cloud_alloc.exception.handler;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExceptionDTO {
    private String message;
    private Exception error;
}
