package westwood222.cloud_alloc.service;

import westwood222.cloud_alloc.dto.upload.UploadRequest;
import westwood222.cloud_alloc.dto.upload.UploadResponse;
import westwood222.cloud_alloc.dto.delete.DeleteRequest;
import westwood222.cloud_alloc.dto.search.SearchRequest;
import westwood222.cloud_alloc.dto.search.SearchResponse;
import westwood222.cloud_alloc.dto.update.UpdateRequest;
import westwood222.cloud_alloc.dto.update.UpdateResponse;

public interface CloudService {
    UploadResponse upload(UploadRequest request) throws Exception;

    SearchResponse search(SearchRequest request) throws Exception;

    UpdateResponse update(UpdateRequest request) throws Exception;

    void delete(DeleteRequest request) throws Exception;
}
