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
import westwood222.cloud_alloc.exception.AccountNotFound;
import westwood222.cloud_alloc.exception.ResourceNotFound;
import westwood222.cloud_alloc.model.File;
import westwood222.cloud_alloc.model.FileProperty;
import westwood222.cloud_alloc.service.resource.ResourceService;
import westwood222.cloud_alloc.service.storage.CloudStorageService;
import westwood222.cloud_alloc.service.account.AccountService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CloudController {
    private final ResourceService resourceService;
    private final AccountService accountService;

    @GetMapping
    SearchResponse allResources(
            @RequestParam(value = "page", required = false) int page,
            @RequestParam(value = "size", required = false) int size,
            @RequestParam(value = "type", required = false) String mineType,
            @RequestParam(value = "name", required = false) String filename
    ) {
        // Build request from params
        // Default is page 1, size 20, with any name and any type
        // The resource service handled the normalization
        FileProperty property = FileProperty.builder()
                .mineType(mineType)
                .filename(filename)
                .build();
        SearchRequest request = SearchRequest.builder()
                .property(property)
                .page(page)
                .size(size)
                .build();

        // Search the local DB for list of file ids
        return resourceService.findAllByProperty(request);
    }

    @GetMapping("{fileId}")
    RedirectView oneResource(@PathVariable("fileId") UUID fileId) throws Exception {
        // Get the account that contains the resource
        File file = resourceService.findOneById(fileId)
                .orElseThrow(() -> new ResourceNotFound(String.format("No resource with id: %s", fileId)));
        UUID accountId = file.getAccount().getId();

        // Get the storageService corresponding to that account
        CloudStorageService storageService = accountService.findOneById(accountId)
                .orElseThrow(() -> new AccountNotFound(String.format("No account with id: %s", accountId)));

        // Get view link from the storage provider
        ViewRequest request = ViewRequest.builder()
                .fileId(file.getForeignId())
                .build();
        ViewResponse response = storageService.view(request);
        return new RedirectView(response.getViewLink());
    }

    @PostMapping
    UploadResponse newResource(@Validated @RequestBody UploadRequest request) throws Exception {
        CloudStorageService service = accountService.getMaxSpace();
        UploadResponse response = service.upload(request);
        accountService.add(service);
        return response;
    }

    @DeleteMapping("{fileId}")
    void deleteResource(
            @PathVariable("fileId") String fileId,
            @RequestParam(value = "hardDelete", defaultValue = "false") boolean isHardDelete
    ) throws Exception {
        CloudStorageService service = accountService.getMaxSpace();
        DeleteRequest request = DeleteRequest.builder().id(fileId).isHardDelete(isHardDelete).build();
        service.delete(request);
        accountService.add(service);
    }
}
