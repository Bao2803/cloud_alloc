package westwood222.cloud_alloc.service.storage.worker;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.service.storage.StorageService;


/**
 * This class provides a way to interact with the cloud storage provider linked to {@code this.account}
 * Solid implementation of this class can upload, delete, and read a resource from the account's Cloud Storage.
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class StorageWorker implements StorageService, Comparable<StorageWorker> {
    @Nonnull
    protected final Account account;    // the account that this StorageWorker is referencing

    protected long freeSpace;           // the current free space in the cloud storage specified by account

    /**
     * Default ordering for service, which is ordered by available space in ascending order.
     * Note: this class has a natural ordering that is inconsistent with equals (i.e. a.compareTo(b) == 0 DOES NOT imply
     * that a.equals(b) == true; 2 different storage can have similar free space).
     *
     * @param otherStorageService the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(
            @Nonnull final StorageWorker otherStorageService
    ) {
        return Long.compare(this.getFreeSpace(), otherStorageService.getFreeSpace());
    }
}
