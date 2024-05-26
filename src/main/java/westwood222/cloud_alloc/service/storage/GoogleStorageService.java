package westwood222.cloud_alloc.service.storage;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import westwood222.cloud_alloc.config.AppConfig;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteRequest;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteResponse;
import westwood222.cloud_alloc.dto.storage.read.StorageReadRequest;
import westwood222.cloud_alloc.dto.storage.read.StorageReadResponse;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadRequest;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadResponse;
import westwood222.cloud_alloc.exception.external.GoogleException;
import westwood222.cloud_alloc.mapper.StorageMapper;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.ResourceProperty;
import westwood222.cloud_alloc.oauth.OAuthProperty;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class GoogleStorageService extends StorageService {
    public static final Map<String, String> OAuthExtraParam = Map.of(
            "access_type", "offline"
    );

    private final Drive service;
    private final StorageMapper storageMapper;

    private GoogleStorageService(Account account, Drive drive, long freeSpace, StorageMapper storageMapper) {
        super(account, freeSpace);
        this.service = drive;
        this.freeSpace = freeSpace;
        this.storageMapper = storageMapper;
    }

    /**
     * Construct an instance of {@link GoogleStorageService}.
     *
     * @param account       contains refresh token, access token, and Provider (should be Google when it arrived here).
     * @param property      clientId and clientSecret for Google.
     * @param storageMapper MapStruct to map Google response to {@link GoogleStorageService}'s DTOs
     * @return a new instance of {@link GoogleStorageService} that points to {@code account}
     */
    public static GoogleStorageService createInstance(
            Account account,
            OAuthProperty property,
            StorageMapper storageMapper
    ) {
        // Get clientId and clientSecret from application.yml
        OAuthProperty.ProviderSecret secret = property.getRegistration().get(account.getProvider().name());
        if (secret == null) {
            throw new RuntimeException("Cannot find OAuth secret for " + account.getProvider());
        }

        // Construct Google's credential; this class will handle refreshing token for us
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

        // Construct Google's Drive object to perform API calls to Google
        Drive service = new Drive.Builder(AppConfig.HTTP_TRANSPORT, AppConfig.JSON_FACTORY, credential)
                .setApplicationName(AppConfig.APPLICATION_NAME)
                .build();

        // Fetch current free space from Drive
        long freeSpace = getFreeSpaceFromDrive(service);

        return new GoogleStorageService(account, service, freeSpace, storageMapper);
    }

    /**
     * Make an API call to Google to calculate the available space in Byte.
     *
     * @param drive the {@link com.google.api.services.drive.model.Drive} that is authenticated
     * @return available space in byte
     */
    private static long getFreeSpaceFromDrive(Drive drive) {
        try {
            About about = drive.about().get()
                    .setFields("storageQuota(limit, usage)")
                    .execute();
            return about.getStorageQuota().getLimit() - about.getStorageQuota().getUsage();
        } catch (IOException e) {
            throw new GoogleException(e);
        }
    }

    /**
     * Update {@link StorageService#freeSpace} by making API call to Google
     */
    private void refreshFreeSpace() {
        this.freeSpace = getFreeSpaceFromDrive(this.service);
    }

    @Override
    public StorageUploadResponse upload(StorageUploadRequest request) {
        String name = request.getFile().getName();
        try {
            InputStreamContent mediaContent = new InputStreamContent(name, request.getFile().getInputStream());
            File result = service.files()
                    .create(
                            new File().setName(name)
                                    .setMimeType(request.getFile().getContentType()),
                            mediaContent
                    )
                    .execute();
            refreshFreeSpace();

            ResourceProperty property = ResourceProperty.builder()
                    .name(result.getName())
                    .mineType(result.getMimeType())
                    .build();
            return storageMapper.toStorageUploadResponse(
                    property,
                    this.getAccount().getProvider(),
                    this.getAccount().getUsername()
            );
        } catch (IOException e) {
            throw new GoogleException(e);
        }
    }

    @Override
    public StorageReadResponse read(StorageReadRequest request) {
        try {
            File result = service.files()
                    .get(request.getForeignId())
                    .setFields("webViewLink")
                    .execute();

            ResourceProperty property = ResourceProperty.builder()
                    .name(result.getName())
                    .mineType(result.getMimeType())
                    .build();
            return storageMapper.toStorageReadResponse(property, result.getWebViewLink());
        } catch (IOException e) {
            throw new GoogleException(e);
        }
    }

    @Override
    public StorageDeleteResponse delete(StorageDeleteRequest request) {
        try {
            if (request.isHardDelete()) {
                service.files().delete(request.getForeignId()).execute();
                return new StorageDeleteResponse();
            }

            File temp = new File();
            temp.setTrashed(true);
            service.files().update(request.getForeignId(), temp).execute();

            // https://developers.google.com/drive/api/guides/delete trash items are removed by Google after 30 days
            int maxTrashTime = 30;
            return storageMapper.toStorageDeleteResponse(LocalDateTime.now().plusDays(maxTrashTime));
        } catch (IOException e) {
            throw new GoogleException(e);
        }
    }
}
