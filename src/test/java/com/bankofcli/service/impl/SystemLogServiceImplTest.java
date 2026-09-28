package com.bankofcli.service.impl;

import com.bankofcli.exception.BankException;
import com.bankofcli.model.SystemLog;
import com.bankofcli.model.User;
import com.bankofcli.repository.contract.SystemLogRepository;
import com.bankofcli.repository.contract.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SystemLogServiceImplTest {

    private SystemLogRepository logRepository;
    private UserRepository userRepository;
    private SystemLogServiceImpl logService;

    @BeforeEach
    void setUp() {

        logRepository = mock(SystemLogRepository.class);
        userRepository = mock(UserRepository.class);

        logService = new SystemLogServiceImpl(
                logRepository,
                userRepository
        );
    }

    // Positive test
    @Test
    void infoShouldSaveLog() {

        User user = new User(
                1L,
                "Bereket",
                "bereket@gmail.com",
                "hash"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        logService.info(
                1L,
                "Login successful"
        );

        verify(logRepository)
                .save(any(SystemLog.class));
    }

    // Negative test
    @Test
    void shouldRejectMissingUser() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                BankException.class,
                () -> logService.info(
                        1L,
                        "Test"
                )
        );

        verify(logRepository, never())
                .save(any(SystemLog.class));
    }
}