package westwood222.cloud_alloc.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.context.annotation.Bean;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.Provider;
import westwood222.cloud_alloc.repository.AccountRepository;

import java.time.LocalDateTime;

@ConfigurationProperties(prefix = "minio")
public class MinIoConfig {
    private final String HOST;
    private final Integer PORT;
    private final String ACCESS;
    private final String PASSWORD;

    @Autowired
    private AccountRepository accountRepository;

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

    @Bean(name = "minioAccount")
    public Account minioAccount() {
        return accountRepository
                .findFirstByProvider(Provider.MINIO)
                .orElseGet(
                        () -> {
                            Account minioAccount = Account.builder()
                                    .provider(Provider.MINIO)
                                    .username(ACCESS)
                                    .refreshToken(PASSWORD)
                                    .expirationDateTime(LocalDateTime.now().plusYears(100))
                                    .build();
                            return accountRepository.save(minioAccount);
                        }
                );
    }
}
