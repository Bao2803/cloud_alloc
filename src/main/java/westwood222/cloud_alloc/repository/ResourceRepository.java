package westwood222.cloud_alloc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import westwood222.cloud_alloc.model.Resource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    Page<Resource> findAllByProperty_NameLikeOrProperty_MimeTypeLike(
            String propertyName,
            String mimeType,
            Pageable pageable
    );

    List<Resource> findAllByUpdatedAtBetween(Instant start, Instant end);
}
