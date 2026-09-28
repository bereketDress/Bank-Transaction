package com.bankofcli.service.contract;
public interface SystemLogService {
    void info(Long userId, String message);
    void error(Long userId, String message);
}
