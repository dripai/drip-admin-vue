package com.drip.admin.infrastructure.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class JobExecutionConfig {
    @Bean
    public ThreadPoolTaskExecutor jobTaskExecutor(
        @Value("${drip.job.worker-pool-size:4}") int workerPoolSize,
        @Value("${drip.job.queue-capacity:100}") int queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(workerPoolSize);
        executor.setMaxPoolSize(workerPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("drip-job-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
