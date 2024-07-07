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
import westwood222.cloud_alloc.dto.storage.read.StorageReadRequest;
import westwood222.cloud_alloc.dto.storage.read.StorageReadResponse;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadRequest;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadResponse;
import westwood222.cloud_alloc.mapper.ResourceMapper;
import westwood222.cloud_alloc.service.resource.ResourceService;
import westwood222.cloud_alloc.service.storage.MinIoService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resource")
public class ResourceController {
    private final ResourceMapper resourceMapper;
    private final ResourceService resourceService;
    private final MinIoService minIoService;

    @GetMapping
    @Operation(summary = "Search for resources")
    ResponseDTO<ResourceSearchResponse> getAllResources(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "type", defaultValue = "") String mimeType,
            @ParameterObject @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ResourceSearchRequest request = resourceMapper.toSearchRequest(pageable, name, mimeType);
        ResourceSearchResponse response = resourceService.search(request);

        return ResponseDTO.success(response);
    }

    @GetMapping("/{resourceId}")
    @Operation(summary = "Get a specific resource")
    ResponseDTO<ResourceReadResponse> getOneResource(
            @PathVariable("resourceId") UUID resourceId
    ) {
        ResourceReadRequest request = resourceMapper.toViewRequest(resourceId);
        ResourceReadResponse response = resourceService.read(request);

        return ResponseDTO.success(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new resource")
    ResponseDTO<ResourceUploadResponse> createResource(
            @RequestPart("files") MultipartFile[] files
    ) {
        ResourceUploadRequest request = resourceMapper.toResourceUploadRequest(files);
        ResourceUploadResponse response = resourceService.upload(request);

        return ResponseDTO.success(response);
    }

    @PostMapping(value = "/test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new resource")
    ResponseDTO<String> createResourceTest(
            @RequestPart("file") MultipartFile file
    ) {
        StorageUploadRequest request = new StorageUploadRequest();
        request.setFile(file);
        StorageUploadResponse response = minIoService.upload(request);

        return ResponseDTO.success(response.getForeignId());
    }

    @GetMapping("/{resourceId}/test")
    @Operation(summary = "Get a specific resource")
    ResponseDTO<String> getOneResourceTest(
            @PathVariable("resourceId") String resourceId
    ) {
        StorageReadRequest request = new StorageReadRequest();
        request.setForeignId(resourceId);
        StorageReadResponse response = minIoService.read(request);

        return ResponseDTO.success(response.getResourceLink());
    }

    @DeleteMapping("/{resourceId}")
    @Operation(summary = "Delete a resource")
    ResponseDTO<ResourceDeleteResponse> deleteResource(
            @PathVariable("resourceId") UUID resourceId,
            @RequestParam(value = "hardDelete", defaultValue = "false") boolean isHardDelete
    ) {
        ResourceDeleteRequest request = resourceMapper.toResourceDeleteRequest(resourceId, isHardDelete);
        ResourceDeleteResponse response = resourceService.delete(request);

        return ResponseDTO.success(response);
    }
}
