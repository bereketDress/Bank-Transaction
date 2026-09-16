package com.bankofcli.service.impl;

import com.bankofcli.exception.BankException;
import com.bankofcli.model.SystemLog;
import com.bankofcli.enums.LogLevel;
import com.bankofcli.repository.SystemLogRepository;
import com.bankofcli.service.SystemLogService;
import java.time.LocalDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemLogServiceImpl implements SystemLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemLogServiceImpl.class);
    private final SystemLogRepository systemLogRepository;

    public SystemLogServiceImpl(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = Objects.requireNonNull(systemLogRepository);
    }

    @Override
    public void info(Long userId, String message) {
        LOGGER.info(message);
        saveLog(userId, LogLevel.INFO, message);
    }

    @Override
    public void error(Long userId, String message) {
        LOGGER.error(message);
        saveLog(userId, LogLevel.ERROR, message);
    }

    private void saveLog(Long userId, LogLevel level, String message) {
        try {
            systemLogRepository.save(
                    new SystemLog(null, level, message, LocalDateTime.now(), userId));
        } catch (BankException exception) {
            LOGGER.error("Could not persist system log", exception);
        }
    }
}
