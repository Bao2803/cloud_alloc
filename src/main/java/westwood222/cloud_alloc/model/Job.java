package westwood222.cloud_alloc.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Job {
    @NotNull
    private Operation operation;

    @NotNull
    private Boolean status;

    @Data
    public static class Operation {
        private Action action;
        private String resourceName;
        private String location;

        public enum Action {
            CREATE, MOVE, DELETE
        }
    }
}
