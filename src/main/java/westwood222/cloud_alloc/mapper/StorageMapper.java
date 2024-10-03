package westwood222.cloud_alloc.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.web.multipart.MultipartFile;
import westwood222.cloud_alloc.dto.storage.manager.delete.ManagerDeleteRequest;
import westwood222.cloud_alloc.dto.storage.manager.delete.ManagerDeleteResponse;
import westwood222.cloud_alloc.dto.storage.manager.read.ManagerReadRequest;
import westwood222.cloud_alloc.dto.storage.manager.upload.ManagerUploadRequest;
import westwood222.cloud_alloc.dto.storage.worker.read.WorkerReadRequest;
import westwood222.cloud_alloc.dto.storage.worker.read.WorkerReadResponse;
import westwood222.cloud_alloc.dto.storage.worker.upload.WorkerUploadResponse;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.ResourceProperty;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StorageMapper {
    @Mapping(source = "files", target = "files")
    ManagerUploadRequest toStorageUploadRequest(MultipartFile[] files);

    @Mapping(target = "account", source = "account")
    @Mapping(source = "property.name", target = "name")
    @Mapping(source = "property.mimeType", target = "mimeType")
    @Mapping(source = "foreignId", target = "foreignId")
    @Mapping(source = "account.provider", target = "provider")
    @Mapping(source = "account.username", target = "username")
    WorkerUploadResponse toStorageUploadResponse(ResourceProperty property, String foreignId, Account account);

    @Mapping(
            source = "accountId",
            target = "accountId",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(source = "foreignId", target = "foreignId")
    ManagerReadRequest toStorageReadRequest(UUID accountId, String foreignId);

    WorkerReadRequest managerReadRequestToWorkerReadRequest(ManagerReadRequest managerRequest);

    @Mapping(source = "resourceLink", target = "resourceLink")
    @Mapping(source = "property.name", target = "resourceName")
    @Mapping(source = "property.mimeType", target = "resourceMimeType")
    WorkerReadResponse toStorageReadResponse(ResourceProperty property, String resourceLink);

    @Mapping(
            source = "accountId",
            target = "accountId",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(source = "foreignId", target = "foreignId")
    @Mapping(source = "isHardDelete", target = "hardDelete")
    ManagerDeleteRequest toStorageDeleteRequest(UUID accountId, String foreignId, boolean isHardDelete);

    @Mapping(source = "deleteDate", target = "deleteDate")
    ManagerDeleteResponse toStorageDeleteResponse(LocalDateTime deleteDate);
}
