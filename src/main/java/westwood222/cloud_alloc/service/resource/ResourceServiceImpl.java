package westwood222.cloud_alloc.service.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import westwood222.cloud_alloc.dto.search.SearchRequest;
import westwood222.cloud_alloc.dto.search.SearchResponse;
import westwood222.cloud_alloc.model.Resource;
import westwood222.cloud_alloc.repository.ResourceRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final ResourceRepository repository;
    private static final short MAX_PAGE_SIZE = 100;

    @Override
    public Resource save(Resource resource) throws IllegalArgumentException {
        return repository.save(resource);
    }

    @Override
    public void deleteById(UUID id) throws IllegalArgumentException {
        repository.delete(Resource.builder().id(id).build());
    }

    @Override
    public Optional<Resource> findOneById(UUID id) throws IllegalArgumentException {
        return repository.findById(id);
    }

    @Override
    public SearchResponse findAllByProperty(SearchRequest request) {
        // Default name to match anything
        String name = request.getResourceProperty().getName();
        if (name == null) {
            name = "%";
        }
        // Default type to match anything
        String type = request.getResourceProperty().getMineType();
        if (type == null) {
            type = "%";
        }
        // Default page to page 1
        int page = request.getPage();
        if (page <= 0) {
            page = 1;
        }
        // Default size to 20; limit size to 100
        int size = request.getSize();
        if (size <= 0) {
            size = 20;
        }
        size = Math.min(size, MAX_PAGE_SIZE);

        // Search for all resources that match the given property
        Pageable pageable = PageRequest.of(page, size);
        Page<Resource> res = repository.findByProperty_MineTypeOrProperty_NameContainingIgnoreCaseOrderByCreatedAtDesc(
                type,
                name,
                pageable
        );
        return SearchResponse.builder()
                .resources(res.getContent())
                .nextPage((page + 1) % res.getTotalPages())     // wrap around if needed
                .nextSize(size)
                .build();
    }
}
