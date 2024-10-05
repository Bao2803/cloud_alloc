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
import westwood222.cloud_alloc.dto.storage.worker.delete.WorkerDeleteRequest;
import westwood222.cloud_alloc.dto.storage.worker.delete.WorkerDeleteResponse;
import westwood222.cloud_alloc.dto.storage.worker.read.WorkerReadRequest;
import westwood222.cloud_alloc.dto.storage.worker.read.WorkerReadResponse;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadRequest;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadResponse;
import westwood222.cloud_alloc.exception.external.GoogleException;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.oauth.OAuthProperty;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.LocalDate;

public class GoogleDriveService extends CloudStorageService {
    // Google removes trash items after 30 days: https://developers.google.com/drive/api/guides/delete
    private static final int MAX_TRASH_DAY = 30;

    private final Drive driveService;

    public GoogleDriveService(@Nonnull Account account, @Nonnull OAuthProperty.ProviderSecret secret) {
        super(account);
        this.driveService = createGoogleDriveSDK(account, secret);
        this.freeSpace = getFreeSpaceFromDrive(driveService);
    }

    private static Drive createGoogleDriveSDK(Account account, OAuthProperty.ProviderSecret secret) {
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
    public long getFreeSpace() {
        return this.freeSpace;
    }

    @Override
    public WorkerUploadResponse upload(WorkerUploadRequest request) {
        try {
            InputStreamContent mediaContent = new InputStreamContent(
                    request.getMimeType(),
                    request.getFile().getInputStream()
            );
            File result = driveService.files()
                    .create(
                            new File().setName(request.getFile().getOriginalFilename())
                                    .setMimeType(request.getMimeType()),
                            mediaContent
                    )
                    .execute();
            refreshFreeSpace();

            return WorkerUploadResponse.builder()
                    .name(result.getName())
                    .mimeType(result.getMimeType())
                    .foreignId(result.getId())
                    .account(account)
                    .username(account.getUsername())
                    .build();
        } catch (IOException e) {
            throw new GoogleException(e);
        }
    }

    @Override
    public WorkerReadResponse read(WorkerReadRequest request) {
        try {
            File result = driveService.files()
                    .get(request.getForeignId())
                    .setFields("webViewLink")
                    .execute();

            return WorkerReadResponse.builder()
                    .resourceName(result.getName())
                    .resourceMimeType(result.getMimeType())
                    .resourceLink(result.getWebViewLink())
                    .build();
        } catch (IOException e) {
            throw new GoogleException(e);
        }
    }

    @Override
    public WorkerDeleteResponse delete(WorkerDeleteRequest request) {
        try {
            if (request.isHardDelete()) {
                driveService.files().delete(request.getForeignId()).execute();
                return WorkerDeleteResponse.builder().deleteDate(LocalDate.now()).build();
            }

            File temp = new File();
            temp.setTrashed(true);
            driveService.files().update(request.getForeignId(), temp).execute();

            return WorkerDeleteResponse.builder().deleteDate(LocalDate.now().plusDays(MAX_TRASH_DAY)).build();
        } catch (IOException e) {
            throw new GoogleException(e);
        }
    }
}
