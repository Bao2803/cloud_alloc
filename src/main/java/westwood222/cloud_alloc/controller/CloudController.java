package westwood222.cloud_alloc.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import westwood222.cloud_alloc.dto.*;
import westwood222.cloud_alloc.service.GoogleService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CloudController {
    private static final Logger log = LoggerFactory.getLogger(CloudController.class);
    private final GoogleService service;

    @GetMapping
    ResponseEntity<SearchResponse> all(
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam Map<String, String> other     // included page and size (stupid spring)
    ) {
        // work around solution
        other.remove("page");
        other.remove("size");

        SearchRequest request = SearchRequest.builder().page(page).size(size).conditions(other).build();
        try {
            SearchResponse response = service.search(request);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Unable to search {}", e.toString());
            return null;
        }
    }

    @GetMapping("{fileId}")
    RedirectView one(@PathVariable("fileId") String fileId) {
        try {
            String viewLink = service.get(fileId);
            return new RedirectView(viewLink);
        } catch (IOException e) {
            log.error("Unable to get file {}: {}", fileId, e.toString());
            return null;
        }
    }

    @PostMapping
    ResponseEntity<UploadResponse> uploadFile(@Validated @RequestBody UploadRequest request) {
        try {
            UploadResponse response = service.upload(request);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Unable to upload file: {} with fileType: {}", request.getFilePath(), request.getFileType());
            return null;
        }
    }
}
