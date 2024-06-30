package westwood222.cloud_alloc.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Job {
    @NotNull
    private Operation operation;

    @NotNull
    private Boolean status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Operation {
        private Action action;
        private String location;
        private String resourceName;

        public enum Action {
            CREATE, MOVE, DELETE
        }
    }

    public void test() {
        System.out.println("HELLOOOOOOO");
    }
}
