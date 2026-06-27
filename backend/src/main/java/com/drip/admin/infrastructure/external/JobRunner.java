package com.drip.admin.infrastructure.external;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.modules.system.entity.SysJobEntity;
import com.drip.admin.modules.system.service.JobService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;

@Component
public class JobRunner {
    private final ThreadPoolTaskExecutor executor;
    private final JobService jobService;
    private final Set<Long> runningJobIds = ConcurrentHashMap.newKeySet();

    public JobRunner(@Qualifier("jobTaskExecutor") ThreadPoolTaskExecutor executor, JobService jobService) {
        this.executor = executor;
        this.jobService = jobService;
    }

    public boolean submit(SysJobEntity job) {
        if (job.getId() == null || !runningJobIds.add(job.getId())) {
            return false;
        }
        try {
            executor.execute(() -> {
                try {
                    jobService.run(job);
                } finally {
                    runningJobIds.remove(job.getId());
                }
            });
            return true;
        } catch (RejectedExecutionException ex) {
            runningJobIds.remove(job.getId());
            throw new BusinessException(500000, "job executor queue is full");
        }
    }
}
