package westwood222.cloud_alloc.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import westwood222.cloud_alloc.oauth.ExtraParamAuthRequestResolver;

@Configuration
public class SecurityConfig {
    private final AuthenticationSuccessHandler handler;
    private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;

    public SecurityConfig(
            ClientRegistrationRepository clientRegistrationRepository,
            @Qualifier("storageWorkerRepository") AuthenticationSuccessHandler handler
    ) {
        this.handler = handler;
        this.authorizationRequestResolver = new ExtraParamAuthRequestResolver(clientRegistrationRepository);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2.authorizationEndpoint(
                                        endPoint -> endPoint.authorizationRequestResolver(
                                                authorizationRequestResolver
                                        )
                                )
                                .successHandler(handler)
                )
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

}