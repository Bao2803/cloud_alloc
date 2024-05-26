package westwood222.cloud_alloc.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.web.multipart.MultipartFile;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteRequest;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteResponse;
import westwood222.cloud_alloc.dto.storage.read.StorageReadRequest;
import westwood222.cloud_alloc.dto.storage.read.StorageReadResponse;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadRequest;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadResponse;
import westwood222.cloud_alloc.model.Provider;
import westwood222.cloud_alloc.model.ResourceProperty;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StorageMapper {
    @Mapping(source = "file", target = "file")
    StorageUploadRequest toStorageUploadRequest(MultipartFile file);

    @Mapping(source = "property.name", target = "name")
    @Mapping(source = "property.mineType", target = "mineType")
    @Mapping(source = "provider", target = "provider")
    @Mapping(source = "username", target = "username")
    StorageUploadResponse toStorageUploadResponse(ResourceProperty property, Provider provider, String username);

    @Mapping(source = "foreignId", target = "foreignId")
    StorageReadRequest toStorageReadRequest(String foreignId);

    @Mapping(source = "resourceLink", target = "resourceLink")
    @Mapping(source = "property.name", target = "resourceName")
    @Mapping(source = "property.mineType", target = "resourceMineType")
    StorageReadResponse toStorageReadResponse(ResourceProperty property, String resourceLink);

    @Mapping(source = "foreignId", target = "foreignId")
    @Mapping(source = "isHardDelete", target = "hardDelete")
    StorageDeleteRequest toStorageDeleteRequest(String foreignId, boolean isHardDelete);

    @Mapping(source = "deleteDate", target = "deleteDate")
    StorageDeleteResponse toStorageDeleteResponse(LocalDateTime deleteDate);
}
