package westwood222.cloud_alloc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import westwood222.cloud_alloc.config.MinIoConfig;

@EnableKafka
@SpringBootApplication
@EnableConfigurationProperties(MinIoConfig.class)
public class CloudAllocApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudAllocApplication.class, args);
    }

}
