package westwood222.cloud_alloc.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.stereotype.Service;
import westwood222.cloud_alloc.dto.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class GoogleService implements CloudService {
    /* Configuration */ // TODO: 1)Need to create new Repo for each account 2)Somehow config in another class?
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

    public GoogleService() throws IOException {
        this.service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    /* Configuration */

    private static Credential getCredential() throws IOException {
        InputStream in = GoogleService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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
    public CreateResponse create(CreateRequest request) {
        return null;
    }

    private String parseQuery(Map<String, String> conditions) {
        final StringBuilder query = new StringBuilder();
        Iterator<String> test = conditions.keySet().iterator();
        while (test.hasNext()) {
            String temp = test.next();
            query.append(temp)
                    .append(" ")
                    .append(conditions.get(temp));

            if (test.hasNext()) {
                query.append(" and ");
            } else {
                break;
            }
        }

        return query.toString();
    }

    @Override
    public SearchResponse search(SearchRequest request) throws IOException {
        final String page = request.getPage();
        final int size = request.getSize();
        final String query = parseQuery(request.getConditions());
        FileList result = service.files().list()
                .setQ(query)
                .setPageSize(size)
                .setPageToken(page)
                .setFields("nextPageToken, files(id)")
                .execute();

        ArrayList<String> files = new ArrayList<>(result.size());
        result.getFiles().forEach(file -> files.add(file.getId()));
        return SearchResponse.builder().ids(files).nextPageToken(result.getNextPageToken()).build();
    }

    public String get(String fileId) throws IOException {
        File result = service.files()
                .get(fileId)
                .setFields("webViewLink")
                .execute();
        return result.getWebViewLink();
    }

    @Override
    public UpdateResponse update(UpdateRequest request) {
        return null;
    }

    @Override
    public DeleteResponse delete(DeleteRequest request) {
        return null;
    }
}
