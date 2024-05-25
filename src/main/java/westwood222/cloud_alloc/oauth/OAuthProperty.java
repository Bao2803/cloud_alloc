package westwood222.cloud_alloc.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class OAuthProperty {
    private Map<String, ProviderSecret> registration;

    @Getter
    @Setter
    public static class ProviderSecret {
        private String clientId;
        private String clientSecret;
        private List<String> scope;
    }
}
