package com.bankofcli.service.impl;

import com.bankofcli.enums.AccountType;
import com.bankofcli.enums.TransactionType;
import com.bankofcli.exception.BankException;
import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;
import com.bankofcli.model.User;
import com.bankofcli.repository.contract.AccountRepository;
import com.bankofcli.repository.contract.TransactionRepository;
import com.bankofcli.service.contract.AccountService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private AccountService accountService;
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {

        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        accountService = mock(AccountService.class);

        transactionService = new TransactionServiceImpl(
                accountRepository,
                transactionRepository,
                accountService
        );
    }

    // =========================
    // DEPOSIT - SUCCESS
    // =========================

    @Test
    void depositShouldWork() {

        User user = new User(
                1L,
                "Bereket",
                "bereket@gmail.com",
                "hash"
        );

        Account account = new Account(
                10L,
                "pinHash",
                AccountType.CHECKING,
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                user
        );

        when(accountService.getOwnedAccount(1L, 10L))
                .thenReturn(account);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.deposit(
                1L,
                10L,
                new BigDecimal("50.00")
        );

        assertEquals(
                new BigDecimal("150.00"),
                account.getBalance()
        );

        assertEquals(
                TransactionType.DEPOSIT,
                result.getTransactionType()
        );

        verify(accountRepository).updateBalance(
                10L,
                new BigDecimal("150.00")
        );

        verify(transactionRepository)
                .save(any(Transaction.class));
    }

    // =========================
    // DEPOSIT - INVALID AMOUNT
    // =========================

    @Test
    void depositShouldRejectInvalidAmount() {

        User user = new User(
                1L,
                "Bereket",
                "bereket@gmail.com",
                "hash"
        );

        Account account = new Account(
                10L,
                "pinHash",
                AccountType.CHECKING,
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                user
        );

        when(accountService.getOwnedAccount(1L, 10L))
                .thenReturn(account);

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.deposit(
                        1L,
                        10L,
                        BigDecimal.ZERO
                )
        );

        verify(accountRepository, never())
                .updateBalance(
                        anyLong(),
                        any(BigDecimal.class)
                );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    // =========================
    // WITHDRAW - SUCCESS
    // =========================

    @Test
    void withdrawShouldWork() {

        User user = new User(
                1L,
                "Bereket",
                "bereket@gmail.com",
                "hash"
        );

        Account account = new Account(
                10L,
                "pinHash",
                AccountType.CHECKING,
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                user
        );

        when(accountService.getOwnedAccount(1L, 10L))
                .thenReturn(account);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.withdraw(
                1L,
                10L,
                new BigDecimal("30.00")
        );

        assertEquals(
                new BigDecimal("70.00"),
                account.getBalance()
        );

        assertEquals(
                TransactionType.WITHDRAWAL,
                result.getTransactionType()
        );

        verify(accountRepository).updateBalance(
                10L,
                new BigDecimal("70.00")
        );

        verify(transactionRepository)
                .save(any(Transaction.class));
    }

    // =========================
    // WITHDRAW - INSUFFICIENT BALANCE
    // =========================

    @Test
    void withdrawShouldRejectInsufficientBalance() {

        User user = new User(
                1L,
                "Bereket",
                "bereket@gmail.com",
                "hash"
        );

        Account account = new Account(
                10L,
                "pinHash",
                AccountType.CHECKING,
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                user
        );

        when(accountService.getOwnedAccount(1L, 10L))
                .thenReturn(account);

        assertThrows(
                IllegalStateException.class,
                () -> transactionService.withdraw(
                        1L,
                        10L,
                        new BigDecimal("200.00")
                )
        );

        verify(accountRepository, never())
                .updateBalance(
                        anyLong(),
                        any(BigDecimal.class)
                );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    // =========================
    // TRANSFER - SUCCESS
    // =========================

    @Test
    void transferShouldWork() {

        User user = new User(
                1L,
                "Bereket",
                "bereket@gmail.com",
                "hash"
        );

        Account source = new Account(
                10L,
                "pinHash",
                AccountType.CHECKING,
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                user
        );

        Account destination = new Account(
                20L,
                "pinHash",
                AccountType.SAVING,
                new BigDecimal("50.00"),
                LocalDateTime.now(),
                user
        );

        when(accountService.getOwnedAccount(1L, 10L))
                .thenReturn(source);

        when(accountRepository.findById(20L))
                .thenReturn(Optional.of(destination));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.transfer(
                1L,
                10L,
                20L,
                new BigDecimal("25.00")
        );

        assertEquals(
                new BigDecimal("75.00"),
                source.getBalance()
        );

        assertEquals(
                new BigDecimal("75.00"),
                destination.getBalance()
        );

        assertEquals(
                TransactionType.TRANSFER,
                result.getTransactionType()
        );

        verify(accountRepository).updateBalance(
                10L,
                new BigDecimal("75.00")
        );

        verify(accountRepository).updateBalance(
                20L,
                new BigDecimal("75.00")
        );

        verify(transactionRepository)
                .save(any(Transaction.class));
    }

    // =========================
    // TRANSFER - SAME ACCOUNT
    // =========================

    @Test
    void transferShouldRejectSameAccount() {

        assertThrows(
                BankException.class,
                () -> transactionService.transfer(
                        1L,
                        10L,
                        10L,
                        new BigDecimal("20.00")
                )
        );

        verify(accountService, never())
                .getOwnedAccount(anyLong(), anyLong());

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    // =========================
    // TRANSFER - DESTINATION NOT FOUND
    // =========================

    @Test
    void transferShouldRejectMissingDestinationAccount() {

        User user = new User(
                1L,
                "Bereket",
                "bereket@gmail.com",
                "hash"
        );

        Account source = new Account(
                10L,
                "pinHash",
                AccountType.CHECKING,
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                user
        );

        when(accountService.getOwnedAccount(1L, 10L))
                .thenReturn(source);

        when(accountRepository.findById(20L))
                .thenReturn(Optional.empty());

        assertThrows(
                BankException.class,
                () -> transactionService.transfer(
                        1L,
                        10L,
                        20L,
                        new BigDecimal("20.00")
                )
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    // =========================
    // TRANSACTION HISTORY
    // =========================

    @Test
    void getTransactionHistoryShouldWork() {

        User user = new User(
                1L,
                "Bereket",
                "bereket@gmail.com",
                "hash"
        );

        Account account = new Account(
                10L,
                "pinHash",
                AccountType.CHECKING,
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                user
        );

        when(accountService.getOwnedAccount(1L, 10L))
                .thenReturn(account);

        when(transactionRepository.findByAccountId(10L, 5))
                .thenReturn(List.of());

        List<Transaction> result =
                transactionService.getTransactionHistory(
                        1L,
                        10L,
                        5
                );

        assertNotNull(result);

        verify(accountService)
                .getOwnedAccount(1L, 10L);

        verify(transactionRepository)
                .findByAccountId(10L, 5);
    }
}