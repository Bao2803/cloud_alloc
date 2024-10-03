package westwood222.cloud_alloc.oauth;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import westwood222.cloud_alloc.model.Provider;

import java.util.List;
import java.util.Map;

@Setter
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class OAuthProperty {
    private Map<String, ProviderSecret> registration;

    @Nonnull
    public ProviderSecret getProviderSecret(Provider provider) {
        return this.registration.get(provider.name().toLowerCase());
    }

    @Getter
    @Setter
    public static class ProviderSecret {
        private String clientId;
        private String clientSecret;
        private List<String> scope;
    }
}
