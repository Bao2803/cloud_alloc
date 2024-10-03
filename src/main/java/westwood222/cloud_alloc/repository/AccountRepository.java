package westwood222.cloud_alloc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.Provider;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    boolean existsByProvider(Provider provider);

    Optional<Account> findFirstByProvider(Provider provider);
}
