package westwood222.cloud_alloc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import westwood222.cloud_alloc.model.Account;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Account findByAvailableSpaceGreaterThanEqualOrderByAvailableSpaceAsc(Long space);
}
