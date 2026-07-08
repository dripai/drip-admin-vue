package com.drip.admin.common.security;

public interface SessionActivityRecorder {
    void touchCurrent(long activeTimeoutSeconds);
}
