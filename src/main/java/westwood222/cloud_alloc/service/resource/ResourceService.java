package westwood222.cloud_alloc.service.resource;

import westwood222.cloud_alloc.dto.search.SearchRequest;
import westwood222.cloud_alloc.dto.search.SearchResponse;
import westwood222.cloud_alloc.model.Resource;

import java.util.Optional;
import java.util.UUID;

/**
 * This service to handle the resources (resources and folders).
 * It interacts directly with the {@link westwood222.cloud_alloc.repository.ResourceRepository} to keep track of
 * the location and metadata of all user' resources and folders.
 */
public interface ResourceService {
    Resource save(Resource resource) throws IllegalArgumentException;

    void deleteById(UUID id) throws IllegalArgumentException;

    Optional<Resource> findOneById(UUID id) throws IllegalArgumentException;

    SearchResponse findAllByProperty(SearchRequest request);
}
