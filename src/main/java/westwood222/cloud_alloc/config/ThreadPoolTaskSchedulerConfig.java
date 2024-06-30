package westwood222.cloud_alloc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ThreadPoolTaskSchedulerConfig {
    private static final int MAX_THREAD = 5;    // # of tasks can be executed concurrently
    private static final String THREAD_ID_PREFIX = "SchedulerService";

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(MAX_THREAD);
        threadPoolTaskScheduler.setThreadNamePrefix(THREAD_ID_PREFIX);
        return threadPoolTaskScheduler;
    }
}
