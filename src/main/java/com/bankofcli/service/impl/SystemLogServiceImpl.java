package com.bankofcli.service.impl;

import com.bankofcli.enums.LogLevel;
import com.bankofcli.exception.BankException;
import com.bankofcli.model.SystemLog;
import com.bankofcli.model.User;
import com.bankofcli.repository.contract.SystemLogRepository;
import com.bankofcli.repository.contract.UserRepository;
import com.bankofcli.service.contract.SystemLogService;

import java.time.LocalDateTime;

public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogRepository logRepository;
    private final UserRepository userRepository;

    public SystemLogServiceImpl(SystemLogRepository logRepository, UserRepository userRepository) {

        this.logRepository = logRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void info(Long userId, String message) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BankException("User not found"));

        SystemLog log = new SystemLog(
                null,
                LogLevel.INFO,
                message,
                LocalDateTime.now(),
                user
        );

        logRepository.save(log);
    }

    @Override
    //orElseThrow() is built-in method from optional class
    public void error(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BankException("User not found"));

        SystemLog log = new SystemLog(
                null,
                LogLevel.ERROR,
                message,
                LocalDateTime.now(),
                user
        );

        logRepository.save(log);
    }
}
