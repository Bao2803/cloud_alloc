package westwood222.cloud_alloc.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.web.multipart.MultipartFile;
import westwood222.cloud_alloc.dto.storage.delete.StorageDeleteRequest;
import westwood222.cloud_alloc.dto.storage.read.StorageReadRequest;
import westwood222.cloud_alloc.dto.storage.upload.StorageUploadRequest;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StorageMapper {
    @Mapping(source = "file", target = "file")
    StorageUploadRequest toStorageUploadRequest(MultipartFile file);

    @Mapping(source = "foreignId", target = "foreignId")
    StorageReadRequest toStorageReadRequest(String foreignId);

    @Mapping(source = "foreignId", target = "foreignId")
    @Mapping(source = "isHardDelete", target = "isHardDelete")
    StorageDeleteRequest toStorageDeleteRequest(String foreignId, boolean isHardDelete);
}
