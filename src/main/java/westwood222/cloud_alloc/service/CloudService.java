package westwood222.cloud_alloc.service;

import westwood222.cloud_alloc.dto.CreateRequest;
import westwood222.cloud_alloc.dto.CreateResponse;
import westwood222.cloud_alloc.dto.DeleteRequest;
import westwood222.cloud_alloc.dto.DeleteResponse;
import westwood222.cloud_alloc.dto.SearchRequest;
import westwood222.cloud_alloc.dto.SearchResponse;
import westwood222.cloud_alloc.dto.UpdateRequest;
import westwood222.cloud_alloc.dto.UpdateResponse;

public interface CloudService {
    CreateResponse create(CreateRequest request) throws Exception;

    SearchResponse search(SearchRequest request) throws Exception;

    UpdateResponse update(UpdateRequest request) throws Exception;

    DeleteResponse delete(DeleteRequest request) throws Exception;
}
