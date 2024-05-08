package westwood222.cloud_alloc.controller;

import lombok.RequiredArgsConstructor;
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
    private final GoogleService service;

    @GetMapping
    SearchResponse all(
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam Map<String, String> other     // included page and size (stupid spring)
    ) throws IOException {
        // work around solution
        other.remove("page");
        other.remove("size");

        SearchRequest request = SearchRequest.builder().page(page).size(size).conditions(other).build();
        return service.search(request);
    }

    @GetMapping("{fileId}")
    RedirectView one(@PathVariable("fileId") String fileId) throws IOException {
        String viewLink = service.get(fileId);
        return new RedirectView(viewLink);
    }

    @PostMapping
    UploadResponse newFile(@Validated @RequestBody UploadRequest request) throws IOException {
        return service.upload(request);
    }

    @DeleteMapping("{fileId}")
    void deleteFile(
            @PathVariable("fileId") String fileId,
            @RequestParam(value = "hardDelete", defaultValue = "false") boolean isHardDelete
    ) throws IOException {
        DeleteRequest request = DeleteRequest.builder().id(fileId).isHardDelete(isHardDelete).build();
        service.delete(request);
    }
}
