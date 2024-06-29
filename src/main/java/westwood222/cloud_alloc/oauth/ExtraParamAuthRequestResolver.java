package westwood222.cloud_alloc.oauth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import westwood222.cloud_alloc.model.Provider;

import java.util.Map;

/**
 * Add extra parameters to the Authorization request in OAuth2 flow.
 * It relies on {@link DefaultOAuth2AuthorizationRequestResolver} to resolve the request, so the behavior is identical.
 */
@Component
public class ExtraParamAuthRequestResolver implements OAuth2AuthorizationRequestResolver {
    // Extra param for each provider
    public static final Map<Provider, Map<String, Object>> PROVIDER_EXTRA_PARAM = Map.of(
            Provider.google, Map.of(
                    "access_type", "offline"
            )
    );

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    @Autowired
    public ExtraParamAuthRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                repo,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        );
    }

    /**
     * Returns the {@link OAuth2AuthorizationRequest} resolved from the provided
     * {@code HttpServletRequest} or {@code null} if not available.
     *
     * @param request the {@code HttpServletRequest}
     * @return the resolved {@link OAuth2AuthorizationRequest} or {@code null} if not
     * available
     */
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(req);
    }

    /**
     * Returns the {@link OAuth2AuthorizationRequest} resolved from the provided
     * {@code HttpServletRequest} or {@code null} if not available.
     *
     * @param request              the {@code HttpServletRequest}
     * @param clientRegistrationId the clientRegistrationId to use
     * @return the resolved {@link OAuth2AuthorizationRequest} or {@code null} if not
     * available
     */
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(req);
    }

    /**
     * Add extra params to the authorization request based on the Provider.
     *
     * @param request The modified {@link OAuth2AuthorizationRequest} with custom request params based on provider.
     *                The provider is obtained using the redirect URI.
     * @return A modified {@link OAuth2AuthorizationRequest} with additional param.
     */
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest request
    ) {
        // Request is null; nothing can be done
        if (request == null) {
            return null;
        }

        // Additional parameters based on provider
        Provider provider = Provider.getProvider(getRegistrationId(request.getRedirectUri()));
        Map<String, Object> additionalParameters = PROVIDER_EXTRA_PARAM.getOrDefault(provider, Map.of());

        return OAuth2AuthorizationRequest
                .from(request)
                .additionalParameters(additionalParameters)
                .build();
    }

    /**
     * Get the registration id using the redirect uri
     *
     * @param redirectURI URI to inspect
     * @return registration id in the redirect URI.
     */
    private String getRegistrationId(String redirectURI) {
        return redirectURI.substring(redirectURI.lastIndexOf('/') + 1);
    }
}
