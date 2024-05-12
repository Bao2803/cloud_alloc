package westwood222.cloud_alloc.service.storage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Service;
import westwood222.cloud_alloc.dto.delete.DeleteRequest;
import westwood222.cloud_alloc.dto.upload.UploadRequest;
import westwood222.cloud_alloc.dto.upload.UploadResponse;
import westwood222.cloud_alloc.dto.view.ViewRequest;
import westwood222.cloud_alloc.dto.view.ViewResponse;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleStorageService implements StorageService {
    /* Configuration */ // TODO: 1)Need to upload new Repo for each account 2)Somehow config in another class?
    private static final String APPLICATION_NAME = "cloud_alloc";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final HttpTransport HTTP_TRANSPORT;
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/google/credentials.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens"; // TODO: database?

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Drive service;

    public GoogleStorageService() throws IOException {
        this.service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    /* Configuration */

    private static Credential getCredential() throws IOException {
        InputStream in = GoogleStorageService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Override
    public int freeSpace() {
        throw new RuntimeException("not implemented");
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
