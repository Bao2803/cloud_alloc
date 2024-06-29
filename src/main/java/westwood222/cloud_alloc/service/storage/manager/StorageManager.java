package westwood222.cloud_alloc.service.storage.manager;

import westwood222.cloud_alloc.repository.StorageWorkerRepository;
import westwood222.cloud_alloc.service.storage.StorageService;

/**
 * This class acts as a proxy for the internal {@code StorageWorker}s by delegating the
 * uploading, deleting, and reading {@link westwood222.cloud_alloc.model.Resource} to {@code StorageWorker}.
 * <p>
 * Typically, it uses {@link StorageWorkerRepository} to obtain the necessary
 * {@code StorageWorker}.
 *
 * @see westwood222.cloud_alloc.service.storage.worker.StorageWorker
 * @see StorageWorkerRepository
 */
public interface StorageManager extends StorageService {
}
