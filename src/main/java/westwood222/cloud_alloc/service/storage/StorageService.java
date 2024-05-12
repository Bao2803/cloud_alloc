package westwood222.cloud_alloc.service.storage;

import westwood222.cloud_alloc.dto.delete.DeleteRequest;
import westwood222.cloud_alloc.dto.search.SearchRequest;
import westwood222.cloud_alloc.dto.search.SearchResponse;
import westwood222.cloud_alloc.dto.upload.UploadRequest;
import westwood222.cloud_alloc.dto.upload.UploadResponse;
import westwood222.cloud_alloc.dto.view.ViewRequest;
import westwood222.cloud_alloc.dto.view.ViewResponse;

public interface CloudStorageService {
    int freeSpace();
    UploadResponse upload(UploadRequest request) throws Exception;

    SearchResponse search(SearchRequest request) throws Exception;

    ViewResponse view(ViewRequest request) throws Exception;

    void delete(DeleteRequest request) throws Exception;
}
