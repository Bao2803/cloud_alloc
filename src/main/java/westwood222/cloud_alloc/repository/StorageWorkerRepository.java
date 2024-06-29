package westwood222.cloud_alloc.repository;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Repository;
import westwood222.cloud_alloc.exception.internal.AccountNotFound;
import westwood222.cloud_alloc.exception.internal.InsufficientStorage;
import westwood222.cloud_alloc.service.storage.worker.StorageWorker;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * This repository keep track of all {@code StorageWorker} available in the application.
 * It also in charge of creating and instantiating the {@code StorageWorker}.
 * <p>
 * This repository is typically used by the {@code StorageManager} to obtains the needed {@code StorageWorker}.
 *
 * @see StorageWorker
 * @see westwood222.cloud_alloc.service.storage.manager.StorageManager
 */
@Repository
public interface StorageWorkerRepository extends AuthenticationSuccessHandler {
    @Nonnull
    StorageWorker getServiceBySpace(long spaceNeed) throws InsufficientStorage;

    @Nonnull
    StorageWorker getServiceById(UUID id) throws AccountNotFound;

    void addService(StorageWorker storageService);
}
