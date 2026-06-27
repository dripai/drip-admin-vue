package com.drip.admin.modules.system.dto;

import java.util.List;

public class JobRunLogQuery extends PageQuery {
    private String jobName;
    private String status;
    private List<String> startedRange;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getStartedRange() {
        return startedRange;
    }

    public void setStartedRange(List<String> startedRange) {
        this.startedRange = startedRange;
    }
}
