package westwood222.cloud_alloc.service.manager;

import org.springframework.stereotype.Service;
import westwood222.cloud_alloc.service.CloudService;

import java.util.Comparator;
import java.util.PriorityQueue;

@Service
public class FreeSpaceServiceManager implements ServiceManager {
    private static final int MINIMUM_SPACE = 1024;
    private static final FreeSpaceServiceManager INSTANCE = new FreeSpaceServiceManager();
    private final PriorityQueue<CloudService> servicePriorityQueue = new PriorityQueue<>(
            Comparator.comparingInt(CloudService::freeSpace)
    );

    public static FreeSpaceServiceManager getInstance() {
        return INSTANCE;
    }

    /**
     * Get the best service for uploading (use available space)
     *
     * @return CloudService that have the largest available space.
     */
    @Override
    public CloudService poll() {
        CloudService service = servicePriorityQueue.peek();
        if (service == null || service.freeSpace() <= MINIMUM_SPACE) {
            // TODO: request more space
            throw new RuntimeException("not implemented requesting extra memory");
        }
        return servicePriorityQueue.poll();
    }

    @Override
    public boolean add(CloudService service) {
        return servicePriorityQueue.add(service);
    }

}
