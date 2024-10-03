package westwood222.cloud_alloc.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import westwood222.cloud_alloc.dto.ResponseDTO;
import westwood222.cloud_alloc.dto.resource.delete.ResourceDeleteRequest;
import westwood222.cloud_alloc.dto.resource.delete.ResourceDeleteResponse;
import westwood222.cloud_alloc.dto.resource.read.ResourceReadRequest;
import westwood222.cloud_alloc.dto.resource.read.ResourceReadResponse;
import westwood222.cloud_alloc.dto.resource.search.ResourceSearchRequest;
import westwood222.cloud_alloc.dto.resource.search.ResourceSearchResponse;
import westwood222.cloud_alloc.dto.resource.upload.ResourceUploadRequest;
import westwood222.cloud_alloc.dto.resource.upload.ResourceUploadResponse;
import westwood222.cloud_alloc.model.ResourceProperty;
import westwood222.cloud_alloc.service.resource.ResourceService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resource")
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping
    @Operation(summary = "Search for resources")
    ResponseDTO<ResourceSearchResponse> getAllResources(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "type", defaultValue = "") String mimeType,
            @ParameterObject @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ResourceProperty searchResourceProperty = ResourceProperty.builder()
                .name(name)
                .mimeType(mimeType)
                .build();
        ResourceSearchRequest request = ResourceSearchRequest.builder()
                .pageable(pageable)
                .resourceProperty(searchResourceProperty)
                .build();

        ResourceSearchResponse response = resourceService.search(request);
        return ResponseDTO.success(response);
    }

    @GetMapping("/{resourceId}")
    @Operation(summary = "Get a specific resource")
    ResponseDTO<ResourceReadResponse> getOneResource(
            @PathVariable("resourceId") UUID resourceId
    ) {
        ResourceReadRequest request = ResourceReadRequest.builder()
                .resourceId(resourceId)
                .build();

        ResourceReadResponse response = resourceService.read(request);
        return ResponseDTO.success(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new resource")
    ResponseDTO<ResourceUploadResponse> createResource(
            @RequestPart("files") MultipartFile[] files
    ) {
        ResourceUploadRequest request = ResourceUploadRequest.builder()
                .files(files)
                .build();

        ResourceUploadResponse response = resourceService.upload(request);
        return ResponseDTO.success(response);
    }

    @DeleteMapping("/{resourceId}")
    @Operation(summary = "Delete a resource")
    ResponseDTO<ResourceDeleteResponse> deleteResource(
            @PathVariable("resourceId") UUID resourceId,
            @RequestParam(value = "hardDelete", defaultValue = "false") boolean isHardDelete
    ) {
        ResourceDeleteRequest request = ResourceDeleteRequest.builder()
                .localId(resourceId)
                .isHardDelete(isHardDelete)
                .build();

        ResourceDeleteResponse response = resourceService.delete(request);
        return ResponseDTO.success(response);
    }
}
