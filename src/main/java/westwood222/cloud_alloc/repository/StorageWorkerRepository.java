package westwood222.cloud_alloc.repository;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import westwood222.cloud_alloc.exception.internal.AccountNotFound;
import westwood222.cloud_alloc.exception.internal.InsufficientStorage;
import westwood222.cloud_alloc.service.storage.worker.CloudStorageService;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * This repository keep track of all {@code CloudStorageService} available in the application.
 * It also in charge of creating and instantiating the {@code CloudStorageService}.
 * <p>
 * This repository is typically used by the {@code StorageManager} to obtains the needed {@code CloudStorageService}.
 *
 * @see CloudStorageService
 * @see westwood222.cloud_alloc.service.storage.manager.StorageManager
 */
public interface StorageWorkerRepository extends AuthenticationSuccessHandler {
    @Nonnull
    CloudStorageService getServiceBySpace(long spaceNeed) throws InsufficientStorage;

    @Nonnull
    CloudStorageService getServiceById(UUID id) throws AccountNotFound;

    void addService(CloudStorageService storageService);
}
