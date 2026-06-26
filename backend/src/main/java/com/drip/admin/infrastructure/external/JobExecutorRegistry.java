package com.drip.admin.infrastructure.external;

import com.drip.admin.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JobExecutorRegistry {
    private final Map<String, Runnable> executors;

    public JobExecutorRegistry() {
        this.executors = Map.of(
            "systemHealthJob.run", this::systemHealth,
            "databaseBackupJob.run", this::databaseBackupMarker
        );
    }

    public void execute(String beanName, String methodName) {
        Runnable executor = executors.get(beanName + "." + methodName);
        if (executor == null) {
            throw new BusinessException(400000, "job executor is not registered");
        }
        executor.run();
    }

    private void systemHealth() {
        // Whitelisted lightweight health task.
    }

    private void databaseBackupMarker() {
        // Database backup is exposed through the dedicated backup API.
    }
}
