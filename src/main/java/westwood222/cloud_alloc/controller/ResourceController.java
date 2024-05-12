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
import westwood222.cloud_alloc.model.Resource;
import westwood222.cloud_alloc.model.ResourceProperty;
import westwood222.cloud_alloc.service.account.AccountService;
import westwood222.cloud_alloc.service.resource.ResourceService;
import westwood222.cloud_alloc.service.storage.StorageService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;
    private final AccountService accountService;

    @GetMapping
    SearchResponse allResources(
            @RequestParam(value = "page", required = false) int page,
            @RequestParam(value = "size", required = false) int size,
            @RequestParam(value = "type", required = false) String mineType,
            @RequestParam(value = "name", required = false) String name
    ) {
        // Build request from params
        // Default is page 1, size 20, with any name and any type
        // The resource service handled the normalization
        ResourceProperty property = ResourceProperty.builder()
                .mineType(mineType)
                .name(name)
                .build();
        SearchRequest request = SearchRequest.builder()
                .resourceProperty(property)
                .page(page)
                .size(size)
                .build();

        // Search the local DB for list of ids
        return resourceService.findAllByProperty(request);
    }

    @GetMapping("{resourceId}")
    RedirectView oneResource(@PathVariable("resourceId") UUID resourceId) throws Exception {
        // Get the account that contains the resource
        Resource resource = resourceService.findOneById(resourceId)
                .orElseThrow(() -> new ResourceNotFound(String.format("No resource with id: %s", resourceId)));
        UUID accountId = resource.getAccount().getId();

        // Get the storageService corresponding to that account
        StorageService storageService = accountService.findOneById(accountId)
                .orElseThrow(() -> new AccountNotFound(String.format("No account with id: %s", accountId)));

        // Get view link from the storage provider
        ViewRequest request = ViewRequest.builder()
                .resourceId(resource.getForeignId())
                .build();
        ViewResponse response = storageService.view(request);
        return new RedirectView(response.getResourceViewLink());
    }

    @PostMapping
    UploadResponse newResource(@Validated @RequestBody UploadRequest request) throws Exception {
        // Get the account with the largest available space
        StorageService storageService = accountService.getMaxSpace();

        // Upload
        UploadResponse response = storageService.upload(request);

        // Add back storage service to the service list
        accountService.add(storageService);

        return response;
    }

    @DeleteMapping("{resourceId}")
    void deleteResource(
            @PathVariable("resourceId") UUID resourceId,
            @RequestParam(value = "hardDelete", defaultValue = "false") boolean isHardDelete
    ) throws Exception {
        // Get the account that contains the resource
        Resource resource = resourceService.findOneById(resourceId)
                .orElseThrow(() -> new ResourceNotFound(String.format("No resource with id: %s", resourceId)));
        UUID accountId = resource.getAccount().getId();

        // Get the storageService corresponding to that account
        StorageService storageService = accountService.findOneById(accountId)
                .orElseThrow(() -> new AccountNotFound(String.format("No account with id: %s", accountId)));

        // Delete resource from the storage provider
        DeleteRequest request = DeleteRequest.builder()
                .resourceId(resource.getForeignId())
                .isHardDelete(isHardDelete)
                .build();
        storageService.delete(request);
    }
}
