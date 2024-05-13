package westwood222.cloud_alloc.service.account;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import westwood222.cloud_alloc.service.storage.StorageService;

import java.util.*;

@Service
public class AccountServiceImpl implements AccountService {
    private final long MINIMUM_SPACE;   // in bytes
    private final Map<UUID, StorageService> serviceMap;
    private final PriorityQueue<StorageService> servicePriorityQueue;

    public AccountServiceImpl(
            Map<UUID, StorageService> serviceMap,
            PriorityQueue<StorageService> servicePriorityQueue,
            @Value("${service.account.min.size.default}") int minSpace
    ) {
        this.serviceMap = serviceMap;
        this.MINIMUM_SPACE = minSpace;
        this.servicePriorityQueue = servicePriorityQueue;
    }

    @Override
    public @NonNull StorageService getMaxSpace(long spaceNeed) {
        spaceNeed = spaceNeed <= 0 ? MINIMUM_SPACE : spaceNeed;

        StorageService service = servicePriorityQueue.peek();
        try {
            if (service == null || service.freeSpace() <= spaceNeed) {
                if (!newAccount()) {
                    throw new RuntimeException("Out of memory but cannot request more account\n");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Objects.requireNonNull(servicePriorityQueue.poll());
    }

    @Override
    public boolean newAccount() {
        return false;
    }

    @Override
    public boolean add(StorageService service) {
        return servicePriorityQueue.add(service);
    }

    @Override
    public Optional<StorageService> getById(UUID id) {
        return Optional.of(serviceMap.get(id));
    }
}
