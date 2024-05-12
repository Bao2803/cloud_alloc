package westwood222.cloud_alloc.service.account;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.service.storage.StorageService;

import java.util.*;

@Service
public class AccountServiceImpl implements AccountService {
    private static final int MINIMUM_SPACE = 1024;

    private final PriorityQueue<StorageService> servicePriorityQueue = new PriorityQueue<>(
            Comparator.comparingInt(StorageService::freeSpace)
    );

    @Override
    public @NonNull StorageService getMaxSpace() {
        StorageService service = servicePriorityQueue.peek();
        if (service == null || service.freeSpace() <= MINIMUM_SPACE) {
            // TODO: request more space
            throw new RuntimeException("not implemented requesting extra memory");
        }
        return Objects.requireNonNull(servicePriorityQueue.poll());
    }

    @Override
    public boolean add(Account account) throws ClassCastException, NullPointerException {
        return false;
    }

    @Override
    public boolean add(StorageService service) {
        return servicePriorityQueue.add(service);
    }

    public Optional<StorageService> findOneById(UUID id) {
        return Optional.empty();
    }
}
