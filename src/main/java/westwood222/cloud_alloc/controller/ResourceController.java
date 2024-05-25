package westwood222.cloud_alloc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
import westwood222.cloud_alloc.mapper.ResourceMapper;
import westwood222.cloud_alloc.service.resource.ResourceService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/resource")
public class ResourceController {
    private final ResourceService resourceService;
    private final ResourceMapper resourceMapper;

    @GetMapping("/")
    ResponseDTO<ResourceSearchResponse> getAllResources(
            @RequestParam(value = "filename", required = false) String filename,
            @RequestParam(value = "type", required = false) String mineType,
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ResourceSearchRequest request = resourceMapper.toSearchRequest(pageable, filename, mineType);
        ResourceSearchResponse response = resourceService.search(request);

        return ResponseDTO.<ResourceSearchResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/{resourceId}")
    ResponseDTO<ResourceReadResponse> getOneResource(
            @PathVariable("resourceId") UUID resourceId
    ) throws Exception {
        ResourceReadRequest request = resourceMapper.toViewRequest(resourceId);
        ResourceReadResponse response = resourceService.read(request);

        return ResponseDTO.<ResourceReadResponse>builder()
                .data(response)
                .build();
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseDTO<ResourceUploadResponse> createResource(
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        ResourceUploadRequest request = resourceMapper.toResourceUploadRequest(file);
        ResourceUploadResponse response = resourceService.upload(request);

        return ResponseDTO.<ResourceUploadResponse>builder()
                .data(response)
                .build();
    }

    @DeleteMapping("/{resourceId}")
    ResponseDTO<ResourceDeleteResponse> deleteResource(
            @PathVariable("resourceId") UUID resourceId,
            @RequestParam(value = "hardDelete", defaultValue = "false") boolean isHardDelete
    ) throws Exception {
        ResourceDeleteRequest request = resourceMapper.toResourceDeleteRequest(resourceId, isHardDelete);
        ResourceDeleteResponse response = resourceService.delete(request);

        return ResponseDTO.<ResourceDeleteResponse>builder()
                .data(response)
                .build();
    }
}
