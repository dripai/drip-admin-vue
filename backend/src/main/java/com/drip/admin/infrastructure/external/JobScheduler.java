package com.drip.admin.infrastructure.external;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.drip.admin.modules.system.entity.SysJobEntity;
import com.drip.admin.modules.system.mapper.SysJobMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobScheduler {
    private final SysJobMapper jobMapper;
    private final JobRunner jobRunner;
    private final Map<Long, LocalDateTime> nextRunTimes = new ConcurrentHashMap<>();

    public JobScheduler(SysJobMapper jobMapper, JobRunner jobRunner) {
        this.jobMapper = jobMapper;
        this.jobRunner = jobRunner;
    }

    @Scheduled(fixedDelayString = "${drip.job.scan-delay-ms:30000}")
    public void scan() {
        LocalDateTime now = LocalDateTime.now();
        for (SysJobEntity job : jobMapper.selectList(new QueryWrapper<SysJobEntity>().eq("status", 1))) {
            LocalDateTime next = nextRunTimes.computeIfAbsent(job.getId(), id -> nextRunTime(job, now));
            if (next != null && !next.isAfter(now)) {
                jobRunner.submit(job);
                nextRunTimes.put(job.getId(), nextRunTime(job, LocalDateTime.now()));
            }
        }
    }

    private static LocalDateTime nextRunTime(SysJobEntity job, LocalDateTime baseTime) {
        return CronExpression.parse(normalizeCron(job.getCronExpression())).next(baseTime);
    }

    private static String normalizeCron(String cron) {
        String trimmed = cron == null ? "" : cron.trim();
        return trimmed.split("\\s+").length == 5 ? "0 " + trimmed : trimmed;
    }
}
