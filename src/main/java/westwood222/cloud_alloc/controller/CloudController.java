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
import westwood222.cloud_alloc.dto.view.ViewRequest;
import westwood222.cloud_alloc.dto.view.ViewResponse;
import westwood222.cloud_alloc.service.CloudService;
import westwood222.cloud_alloc.service.manager.ServiceManager;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CloudController {
    private final ServiceManager serviceManager;

    @GetMapping
    SearchResponse all(
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam Map<String, String> other     // included page and size (stupid spring)
    ) throws Exception {
        // work around solution
        other.remove("page");
        other.remove("size");

        CloudService service = serviceManager.poll();
        SearchRequest request = SearchRequest.builder().page(page).size(size).conditions(other).build();
        SearchResponse response = service.search(request);
        serviceManager.add(service);
        return response;
    }

    @GetMapping("{fileId}")
    RedirectView one(@PathVariable("fileId") String fileId) throws Exception {
        CloudService service = serviceManager.poll();
        ViewRequest request = ViewRequest.builder().fileId(fileId).build();
        ViewResponse response = service.view(request);
        serviceManager.add(service);
        return new RedirectView(response.getViewLink());
    }

    @PostMapping
    UploadResponse newFile(@Validated @RequestBody UploadRequest request) throws Exception {
        CloudService service = serviceManager.poll();
        UploadResponse response = service.upload(request);
        serviceManager.add(service);
        return response;
    }

    @DeleteMapping("{fileId}")
    void deleteFile(
            @PathVariable("fileId") String fileId,
            @RequestParam(value = "hardDelete", defaultValue = "false") boolean isHardDelete
    ) throws Exception {
        CloudService service = serviceManager.poll();
        DeleteRequest request = DeleteRequest.builder().id(fileId).isHardDelete(isHardDelete).build();
        service.delete(request);
        serviceManager.add(service);
    }
}
