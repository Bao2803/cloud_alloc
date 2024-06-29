package westwood222.cloud_alloc.service.storage.worker;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import westwood222.cloud_alloc.config.GoogleConfig;
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

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.time.LocalDateTime;

public class GoogleStorageWorker extends StorageWorker {
    private final Drive driveService;
    private final StorageMapper storageMapper;
    private final FileNameMap fileNameMap = URLConnection.getFileNameMap();

    public GoogleStorageWorker(
            @Nonnull Account account,
            @Nonnull OAuthProperty.ProviderSecret secret,
            StorageMapper storageMapper
    ) {
        super(account);
        this.storageMapper = storageMapper;
        this.driveService = createGoogleDriveSDK(account, secret);
        this.freeSpace = getFreeSpaceFromDrive(driveService);
    }

    private static Drive createGoogleDriveSDK(
            Account account,
            OAuthProperty.ProviderSecret secret
    ) {
        // Construct Google's credential; this class will handle refreshing token for us
        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(GoogleConfig.HTTP_TRANSPORT)
                .setJsonFactory(GoogleConfig.JSON_FACTORY)
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
        return new Drive.Builder(GoogleConfig.HTTP_TRANSPORT, GoogleConfig.JSON_FACTORY, credential)
                .setApplicationName(GoogleConfig.APPLICATION_NAME)
                .build();
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
     * Update the parent's freeSpace by making API call to Google
     */
    private void refreshFreeSpace() {
        this.freeSpace = getFreeSpaceFromDrive(this.driveService);
    }

    @Override
    public StorageUploadResponse upload(StorageUploadRequest request) {
        String name = request.getFile().getOriginalFilename();
        try {
            String mineType = fileNameMap.getContentTypeFor(request.getFile().getOriginalFilename());
            InputStreamContent mediaContent = new InputStreamContent(mineType, request.getFile().getInputStream());
            File result = driveService.files()
                    .create(
                            new File().setName(name)
                                    .setMimeType(mineType),
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
                    result.getId(),
                    account
            );
        } catch (IOException e) {
            throw new GoogleException(e);
        }
    }

    @Override
    public StorageReadResponse read(StorageReadRequest request) {
        try {
            File result = driveService.files()
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
                driveService.files().delete(request.getForeignId()).execute();
                return storageMapper.toStorageDeleteResponse(LocalDateTime.now());
            }

            File temp = new File();
            temp.setTrashed(true);
            driveService.files().update(request.getForeignId(), temp).execute();

            // Google removes trash items after 30 days: https://developers.google.com/drive/api/guides/delete
            int maxTrashTime = 30;
            return storageMapper.toStorageDeleteResponse(LocalDateTime.now().plusDays(maxTrashTime));
        } catch (IOException e) {
            throw new GoogleException(e);
        }
    }
}
