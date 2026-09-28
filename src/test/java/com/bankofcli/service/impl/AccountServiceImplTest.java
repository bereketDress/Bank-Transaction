package com.bankofcli.service.impl;

import com.bankofcli.enums.AccountType;
import com.bankofcli.exception.BankException;
import com.bankofcli.model.Account;
import com.bankofcli.model.User;
import com.bankofcli.repository.contract.AccountRepository;
import com.bankofcli.service.contract.UserService;
import com.bankofcli.util.PasswordHasher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    private AccountRepository accountRepository;
    private UserService userService;
    private PasswordHasher passwordHasher;
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {

        accountRepository = mock(AccountRepository.class);
        userService = mock(UserService.class);
        passwordHasher = mock(PasswordHasher.class);

        accountService = new AccountServiceImpl(
                accountRepository,
                userService,
                passwordHasher
        );
    }

    // Positive test
    @Test
    void createAccountShouldWork() {

        User user = new User(
                1L,
                "Bereket",
                "bereket@gmail.com",
                "passwordHash"
        );

        when(userService.getUser(1L))
                .thenReturn(user);

        when(passwordHasher.hash("1234"))
                .thenReturn("hashedPin");

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(i -> i.getArgument(0));

        Account account = accountService.createAccount(
                1L,
                "1234",
                AccountType.CHECKING
        );

        assertEquals(AccountType.CHECKING, account.getType());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals("hashedPin", account.getPinHash());

        verify(accountRepository).save(any(Account.class));
    }

    // Negative test
    @Test
    void createAccountShouldRejectInvalidPin() {

        User user = new User(
                1L,
                "Bereket",
                "bereket@gmail.com",
                "passwordHash"
        );

        when(userService.getUser(1L))
                .thenReturn(user);

        assertThrows(
                BankException.class,
                () -> accountService.createAccount(
                        1L,
                        "12",
                        AccountType.CHECKING
                )
        );
    }
}