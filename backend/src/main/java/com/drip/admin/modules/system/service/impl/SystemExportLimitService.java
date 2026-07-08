package com.drip.admin.modules.system.service.impl;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.modules.system.service.ConfigService;
import org.springframework.stereotype.Service;

@Service
public class SystemExportLimitService {
    public static final String EXPORT_MAX_ROWS_KEY = "system.export.maxRows";
    private static final int DEFAULT_MAX_ROWS = 10000;

    private final ConfigService configService;

    public SystemExportLimitService(ConfigService configService) {
        this.configService = configService;
    }

    public int maxRows() {
        String value = configService.valueOrDefault(EXPORT_MAX_ROWS_KEY, String.valueOf(DEFAULT_MAX_ROWS));
        try {
            int maxRows = Integer.parseInt(value);
            if (maxRows <= 0) throw new NumberFormatException("maxRows must be positive");
            return maxRows;
        } catch (NumberFormatException ex) {
            throw new BusinessException(500000, "system export max rows config invalid");
        }
    }
}
