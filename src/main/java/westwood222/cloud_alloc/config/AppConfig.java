package westwood222.cloud_alloc.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Getter
@Configuration
public class AppConfig {
    public static final List<String> GOOGLE_SCOPE = Collections.singletonList(DriveScopes.DRIVE);
    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public static final HttpTransport HTTP_TRANSPORT;
    public static String APPLICATION_NAME;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    public void setApplicationName(
            @Value("${spring.application.name}") String name) {
        APPLICATION_NAME = name;
    }
}
