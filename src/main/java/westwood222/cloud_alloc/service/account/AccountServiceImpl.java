package westwood222.cloud_alloc.service.manager;

import org.springframework.stereotype.Service;
import westwood222.cloud_alloc.service.storage.CloudStorageService;

import java.util.Comparator;
import java.util.PriorityQueue;

@Service
public class ManagerServiceImpl implements ManagerService {
    private static final int MINIMUM_SPACE = 1024;
    private static final ManagerServiceImpl INSTANCE = new ManagerServiceImpl();
    private final PriorityQueue<CloudStorageService> servicePriorityQueue = new PriorityQueue<>(
            Comparator.comparingInt(CloudStorageService::freeSpace)
    );

    public static ManagerServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public CloudStorageService getMaxSpace() {
        CloudStorageService service = servicePriorityQueue.peek();
        if (service == null || service.freeSpace() <= MINIMUM_SPACE) {
            // TODO: request more space
            throw new RuntimeException("not implemented requesting extra memory");
        }
        return servicePriorityQueue.poll();
    }

    @Override
    public CloudStorageService getContainer(String fileId) {
        return null;
    }

    @Override
    public boolean add(CloudStorageService service) {
        return servicePriorityQueue.add(service);
    }

}
