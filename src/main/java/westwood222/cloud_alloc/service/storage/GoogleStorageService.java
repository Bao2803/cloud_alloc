package westwood222.cloud_alloc.service.storage;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import westwood222.cloud_alloc.config.AppConfig;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteRequest;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteResponse;
import westwood222.cloud_alloc.dto.storage.read.StorageReadRequest;
import westwood222.cloud_alloc.dto.storage.read.StorageReadResponse;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadRequest;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadResponse;
import westwood222.cloud_alloc.exception.external.ExternalException;
import westwood222.cloud_alloc.exception.external.GoogleException;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.oauth.OAuthProperty;

import java.io.IOException;
import java.util.Map;

public class GoogleStorageService extends StorageService {
    public static final Map<String, String> OAuthExtraParam = Map.of(
            "access_type", "offline"
    );

    private final Drive service;

    private GoogleStorageService(Account account, Drive drive, long freeSpace) {
        super(account, freeSpace);
        this.service = drive;
        this.freeSpace = freeSpace;
    }

    public static GoogleStorageService createInstance(Account account, OAuthProperty property) {
        OAuthProperty.ProviderSecret secret = property.getRegistration().get(account.getProvider().name());
        if (secret == null) {
            throw new RuntimeException("Cannot find OAuth secret for " + account.getProvider());
        }

        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(AppConfig.HTTP_TRANSPORT)
                .setJsonFactory(AppConfig.JSON_FACTORY)
                .setTokenServerUrl(new GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL))
                .setClientAuthentication(
                        new ClientParametersAuthentication(
                                secret.getClientId(),
                                secret.getClientSecret()
                        )
                )
                .build()
                .setRefreshToken(account.getRefreshToken())
                .setAccessToken(account.getAccessToken());

        Drive service = new Drive.Builder(AppConfig.HTTP_TRANSPORT, AppConfig.JSON_FACTORY, credential)
                .setApplicationName(AppConfig.APPLICATION_NAME)
                .build();

        long freeSpace = getFreeSpaceFromDrive(service);

        return new GoogleStorageService(account, service, freeSpace);
    }

    private static long getFreeSpaceFromDrive(Drive drive) throws ExternalException {
        About about;
        try {
            about = drive.about().get()
                    .setFields("storageQuota(limit, usage)")
                    .execute();
        } catch (IOException e) {
            throw new GoogleException(e);
        }
        return about.getStorageQuota().getLimit() - about.getStorageQuota().getUsage();
    }

    private void refreshFreeSpace() {
        this.freeSpace = getFreeSpaceFromDrive(this.service);
    }

    @Override
    public StorageUploadResponse upload(StorageUploadRequest request) throws ExternalException {
//        try (InputStream inputFile = new BufferedInputStream(new FileInputStream(request.getResourcePath()))) {
//            InputStreamContent mediaContent = new InputStreamContent(request.getResourceProperty().getName(), inputFile);
//            File result;
//            try {
//                result = service.files()
//                        .create(
//                                new File()
//                                        .setName(request.getResourceProperty().getName())
//                                        .setMimeType(request.getResourceProperty().getMineType()),
//                                mediaContent)
//                        .execute();
//
//                refreshFreeSpace();
//            } catch (IOException e) {
//                throw new GoogleException(e);
//            }
//            return ResourceUploadResponse.builder().resourceId(result.getId()).build();
//        }
        return null;
    }

    @Override
    public StorageReadResponse read(StorageReadRequest request) throws ExternalException {
//        File result = service.files()
//                .get(request.getResourceId())
//                .setFields("webViewLink")
//                .execute();
//        return ResourceReadResponse.builder().resourceViewLink(result.getWebViewLink()).build();
        return null;
    }

    @Override
    public StorageDeleteResponse delete(StorageDeleteRequest request) {
//        ResourceDeleteResponse response = new ResourceDeleteResponse();
//        try {
//            if (request.isHardDelete()) {
//                service.files().delete(request.getResourceId()).execute();
//            } else {
//                File temp = new File();
//                temp.setTrashed(true);
//                service.files().update(request.getResourceId(), temp).execute();
//            }
//        } catch (IOException e) {
//            throw new GoogleException(e);
//        }
//        return response;
        return null;
    }
}
