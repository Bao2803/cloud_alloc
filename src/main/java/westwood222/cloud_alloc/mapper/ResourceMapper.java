package westwood222.cloud_alloc.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import westwood222.cloud_alloc.dto.resource.delete.ResourceDeleteRequest;
import westwood222.cloud_alloc.dto.resource.delete.ResourceDeleteResponse;
import westwood222.cloud_alloc.dto.resource.read.ResourceReadRequest;
import westwood222.cloud_alloc.dto.resource.read.ResourceReadResponse;
import westwood222.cloud_alloc.dto.resource.search.ResourceSearchRequest;
import westwood222.cloud_alloc.dto.resource.search.ResourceSearchResponse;
import westwood222.cloud_alloc.dto.resource.upload.ResourceUploadRequest;
import westwood222.cloud_alloc.dto.resource.upload.ResourceUploadResponse;
import westwood222.cloud_alloc.dto.storage.manager.delete.ManagerDeleteResponse;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadResponse;
import westwood222.cloud_alloc.model.Resource;

import java.util.List;
import java.util.UUID;

@Component
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ResourceMapper {
    @Mapping(source = "pageable", target = "pageable")
    @Mapping(source = "filename", target = "resourceProperty.name", defaultValue = "%")
    @Mapping(source = "mimeType", target = "resourceProperty.mimeType", defaultValue = "%")
    ResourceSearchRequest toSearchRequest(Pageable pageable, String filename, String mimeType);

    @Mapping(source = "nextPage", target = "nextPage")
    @Mapping(source = "nextSize", target = "nextSize")
    @Mapping(source = "totalPage", target = "totalPage")
    @Mapping(source = "resources", target = "resources")
    ResourceSearchResponse toSearchResponse(int nextPage, int nextSize, int totalPage, List<ResourceReadResponse> resources);

    @Mapping(source = "resourceId", target = "resourceId")
    ResourceReadRequest toViewRequest(UUID resourceId);

    @Mapping(source = "files", target = "files")
    ResourceUploadRequest toResourceUploadRequest(MultipartFile[] files);

    @Mapping(source = "localId", target = "resourceId")
    @Mapping(source = "storageResponse.provider", target = "provider")
    @Mapping(source = "storageResponse.username", target = "username")
    ResourceUploadResponse.File toResourceUploadResponse(UUID localId, WorkerUploadResponse storageResponse);

    @Mapping(source = "localId", target = "localId")
    @Mapping(source = "isHardDelete", target = "hardDelete")
    ResourceDeleteRequest toResourceDeleteRequest(UUID localId, boolean isHardDelete);

    @Mapping(target = "resourceLink", ignore = true)
    @Mapping(source = "resource.id", target = "resourceId")
    @Mapping(source = "resource.account.username", target = "username")
    @Mapping(source = "resource.account.provider", target = "provider")
    @Mapping(source = "resource.property.name", target = "resourceName")
    @Mapping(source = "resource.property.mimeType", target = "resourceMimeType")
    ResourceReadResponse resourceToResourceReadResponse(Resource resource);

    @Mapping(source = "resource.id", target = "resourceId")
    @Mapping(source = "resourceLink", target = "resourceLink")
    @Mapping(source = "resource.account.username", target = "username")
    @Mapping(source = "resource.account.provider", target = "provider")
    @Mapping(source = "resource.property.name", target = "resourceName")
    @Mapping(source = "resource.property.mimeType", target = "resourceMimeType")
    ResourceReadResponse toResourceReadResponse(Resource resource, String resourceLink);

    @Mapping(source = "deleteDate", target = "deleteDate")
    ResourceDeleteResponse storageDeleteResponsetoResourceDeleteResponse(ManagerDeleteResponse storageResponse);
}
