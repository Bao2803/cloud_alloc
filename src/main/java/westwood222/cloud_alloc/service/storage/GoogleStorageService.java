package westwood222.cloud_alloc.service.storage;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import westwood222.cloud_alloc.config.AppConfig;
import westwood222.cloud_alloc.dto.delete.DeleteRequest;
import westwood222.cloud_alloc.dto.upload.UploadRequest;
import westwood222.cloud_alloc.dto.upload.UploadResponse;
import westwood222.cloud_alloc.dto.view.ViewRequest;
import westwood222.cloud_alloc.dto.view.ViewResponse;
import westwood222.cloud_alloc.model.Account;

import java.io.*;

public class GoogleStorageService implements StorageService {
    private static final String CREDENTIALS_FILE_PATH = "/google/credentials.json";

    private final Drive service;

    private GoogleStorageService(Drive service) {
        this.service = service;
    }

    public static GoogleStorageService createInstance(Account account) throws IOException {
        InputStream in = GoogleStorageService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(AppConfig.JSON_FACTORY, new InputStreamReader(in));
        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(AppConfig.HTTP_TRANSPORT)
                .setJsonFactory(AppConfig.JSON_FACTORY)
                .setTokenServerUrl(new GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL))
                .setClientAuthentication(new ClientParametersAuthentication(
                        clientSecrets.getDetails().getClientId(),
                        clientSecrets.getDetails().getClientSecret())
                )
                .build()
                .setRefreshToken(account.getRefreshToken())
                .setExpirationTimeMilliseconds(0L)
                .setExpiresInSeconds(0L)
                .setAccessToken(null);

        Drive service = new Drive.Builder(AppConfig.HTTP_TRANSPORT, AppConfig.JSON_FACTORY, credential)
                .setApplicationName(AppConfig.APPLICATION_NAME)
                .build();
        return new GoogleStorageService(service);
    }

    @Override
    public long freeSpace() throws IOException {
        About about = service.about().get()
                .setFields("storageQuot(limit, usage)")
                .execute();
        return about.getMaxUploadSize();
    }

    @Override
    public UploadResponse upload(UploadRequest request) throws IOException {
        try (InputStream inputFile = new BufferedInputStream(new FileInputStream(request.getResourcePath()))) {
            InputStreamContent mediaContent = new InputStreamContent(request.getResourceProperty().getName(), inputFile);
            File result = service.files()
                    .create(new File().setName(request.getResourceProperty().getName()), mediaContent)
                    .execute();
            return UploadResponse.builder().resourceId(result.getId()).build();
        }
    }

    @Override
    public ViewResponse view(ViewRequest request) throws IOException {
        File result = service.files()
                .get(request.getResourceId())
                .setFields("webViewLink")
                .execute();
        return ViewResponse.builder().resourceViewLink(result.getWebViewLink()).build();
    }

    @Override
    public void delete(DeleteRequest request) throws IOException {
        if (request.isHardDelete()) {
            service.files().delete(request.getResourceId()).execute();
        } else {
            File temp = new File();
            temp.setTrashed(true);
            service.files().update(request.getResourceId(), temp).execute();
        }
    }
}
