package westwood222.cloud_alloc.oauth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import westwood222.cloud_alloc.model.Provider;
import westwood222.cloud_alloc.service.storage.GoogleStorageService;

import java.util.HashMap;
import java.util.Map;

public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                repo,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(req);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(req);
    }

    /**
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
        Map<String, Object> additionalParameters = new HashMap<>();
        Provider provider = Provider.getProvider(getRegistrationId(request.getRedirectUri()));
        switch (provider) {
            case google -> additionalParameters.putAll(GoogleStorageService.OAuthExtraParam);
            case microsoft, dropbox -> throw new RuntimeException("Not implemented");
        }

        return OAuth2AuthorizationRequest
                .from(request)
                .additionalParameters(additionalParameters)
                .build();
    }

    /**
     * Obtain the registration id using the redirect uri
     *
     * @param redirectURI URI to inspect
     * @return registration id in the redirect URI.
     */
    private String getRegistrationId(String redirectURI) {
        return redirectURI.substring(redirectURI.lastIndexOf('/') + 1);
    }
}
