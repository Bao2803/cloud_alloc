package westwood222.cloud_alloc.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import westwood222.cloud_alloc.model.Account;
import westwood222.cloud_alloc.model.Provider;
import westwood222.cloud_alloc.repository.AccountRepository;
import westwood222.cloud_alloc.service.storage.GoogleStorageService;
import westwood222.cloud_alloc.service.storage.StorageService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class AccountServiceConfig {
    // Map of concrete implementation of CloudStorageService and their corresponding provider
    public static final Map<Provider, Class<? extends StorageService>> PROVIDER_CLASS_MAP = Map.of(
            Provider.Google, GoogleStorageService.class
    );

    private final AccountRepository repository;

    @Bean("accountList")
    public List<Account> accountList() {
        return repository.findAll();
    }

    @Bean
    @DependsOn({"accountList"})
    public Map<UUID, StorageService> storageServiceMap(
            List<Account> accounts
    ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<UUID, StorageService> storageServiceMap = new HashMap<>(accounts.size());

        Method factoryMethod;
        for (Account account : accounts) {
            factoryMethod = PROVIDER_CLASS_MAP.get(account.getProvider()).getMethod(
                    "createInstance",
                    Account.class
            );
            storageServiceMap.put(account.getId(), (StorageService) factoryMethod.invoke(null, account));
        }

        return storageServiceMap;
    }

    @Bean
    @DependsOn({"accountList"})
    public PriorityQueue<StorageService> storageServicePriorityQueue(List<Account> accounts) {
        int initialSize = Math.max(accounts.size(), 11);    // default capacity for PQ is 11
        return new PriorityQueue<>(initialSize, Comparator.comparingLong((StorageService storageService) -> {
            try {
                return storageService.freeSpace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
