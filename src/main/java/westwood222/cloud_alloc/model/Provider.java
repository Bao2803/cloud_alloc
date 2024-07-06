package westwood222.cloud_alloc.model;

/**
 * The enum values must match the those under security.oauth2.client.registration
 * This enum will be used as registrationId to look up the
 * {@link org.springframework.security.oauth2.client.registration.ClientRegistration}
 * using {@link org.springframework.security.oauth2.client.registration.ClientRegistrationRepository}
 */
public enum Provider {
    GOOGLE,
    MICROSOFT,
    DROPBOX,
    ;

    public static Provider getProvider(String provider) throws IllegalArgumentException {
        try {
            return Provider.valueOf(provider.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported provider", e);
        }
    }
}
