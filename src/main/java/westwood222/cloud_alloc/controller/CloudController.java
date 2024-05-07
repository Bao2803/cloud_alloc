package westwood222.cloud_alloc.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import westwood222.cloud_alloc.dto.delete.DeleteRequest;
import westwood222.cloud_alloc.dto.search.SearchRequest;
import westwood222.cloud_alloc.dto.search.SearchResponse;
import westwood222.cloud_alloc.dto.upload.UploadRequest;
import westwood222.cloud_alloc.dto.upload.UploadResponse;
import westwood222.cloud_alloc.service.GoogleService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CloudController {
    private static final Logger log = LoggerFactory.getLogger(CloudController.class);
    private final GoogleService service;

    @GetMapping
    SearchResponse all(
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam Map<String, String> other     // included page and size (stupid spring)
    ) {
        // work around solution
        other.remove("page");
        other.remove("size");

        SearchRequest request = SearchRequest.builder().page(page).size(size).conditions(other).build();
        try {
            return service.search(request);
        } catch (IOException e) {
            log.error("Unable to search\n {}", e.toString());
            return null;
        }
    }

    @GetMapping("{fileId}")
    RedirectView one(@PathVariable("fileId") String fileId) {
        try {
            String viewLink = service.get(fileId);
            return new RedirectView(viewLink);
        } catch (IOException e) {
            log.error("Unable to get file {}\n {}", fileId, e.toString());
            return null;
        }
    }

    @PostMapping
    UploadResponse newFile(@Validated @RequestBody UploadRequest request) {
        try {
            return service.upload(request);
        } catch (IOException e) {
            log.error("Unable to upload file: {} with fileType: {}\n {}", request.getFilePath(), request.getFileType(), e.toString());
            return null;
        }
    }

    @DeleteMapping("{fileId}")
    void deleteFile(
            @PathVariable("fileId") String fileId,
            @RequestParam(value = "hardDelete", defaultValue = "false") boolean isHardDelete
    ) {
        try {
            DeleteRequest request = DeleteRequest.builder().id(fileId).isHardDelete(isHardDelete).build();
            service.delete(request);
        } catch (IOException e) {
            log.error("Can't delete file: {}\n {}", fileId, e.toString());
        }
    }
}
