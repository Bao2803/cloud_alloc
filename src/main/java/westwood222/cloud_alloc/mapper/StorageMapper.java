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
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.ResourceProperty;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StorageMapper {
    @Mapping(source = "file", target = "file")
    StorageUploadRequest toStorageUploadRequest(MultipartFile file);

    @Mapping(target = "account", source = "account")
    @Mapping(source = "property.name", target = "name")
    @Mapping(source = "property.mineType", target = "mineType")
    @Mapping(source = "foreignId", target = "foreignId")
    @Mapping(source = "account.provider", target = "provider")
    @Mapping(source = "account.username", target = "username")
    StorageUploadResponse toStorageUploadResponse(ResourceProperty property, String foreignId, Account account);

    @Mapping(
            source = "accountId",
            target = "accountId",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(source = "foreignId", target = "foreignId")
    StorageReadRequest toStorageReadRequest(UUID accountId, String foreignId);

    @Mapping(source = "resourceLink", target = "resourceLink")
    @Mapping(source = "property.name", target = "resourceName")
    @Mapping(source = "property.mineType", target = "resourceMineType")
    StorageReadResponse toStorageReadResponse(ResourceProperty property, String resourceLink);


    @Mapping(
            source = "accountId",
            target = "accountId",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(source = "foreignId", target = "foreignId")
    @Mapping(source = "isHardDelete", target = "hardDelete")
    StorageDeleteRequest toStorageDeleteRequest(UUID accountId, String foreignId, boolean isHardDelete);

    @Mapping(source = "deleteDate", target = "deleteDate")
    StorageDeleteResponse toStorageDeleteResponse(LocalDateTime deleteDate);
}
