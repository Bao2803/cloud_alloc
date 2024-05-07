package westwood222.cloud_alloc.service;

import westwood222.cloud_alloc.dto.UploadRequest;
import westwood222.cloud_alloc.dto.UploadResponse;
import westwood222.cloud_alloc.dto.DeleteRequest;
import westwood222.cloud_alloc.dto.SearchRequest;
import westwood222.cloud_alloc.dto.SearchResponse;
import westwood222.cloud_alloc.dto.UpdateRequest;
import westwood222.cloud_alloc.dto.UpdateResponse;

public interface CloudService {
    UploadResponse upload(UploadRequest request) throws Exception;

    SearchResponse search(SearchRequest request) throws Exception;

    UpdateResponse update(UpdateRequest request) throws Exception;

    void delete(DeleteRequest request) throws Exception;
}
