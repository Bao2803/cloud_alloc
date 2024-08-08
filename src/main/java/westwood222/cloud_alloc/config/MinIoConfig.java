package westwood222.cloud_alloc.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.context.annotation.Bean;

@ConfigurationProperties(prefix = "minio")
public class MinIoConfig {
    private final String HOST;
    private final Integer PORT;
    private final String ACCESS;
    private final String PASSWORD;

    @ConstructorBinding
    public MinIoConfig(
            String HOST,
            Integer PORT,
            String ACCESS,
            String PASSWORD
    ) {
        this.HOST = HOST;
        this.PORT = PORT;
        this.ACCESS = ACCESS;
        this.PASSWORD = PASSWORD;
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(HOST, PORT, false)
                .credentials(ACCESS, PASSWORD)
                .build();
    }
}
