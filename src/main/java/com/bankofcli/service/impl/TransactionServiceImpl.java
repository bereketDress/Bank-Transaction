package com.bankofcli.service.impl;

import com.bankofcli.exception.BankException;
import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;
import com.bankofcli.enums.TransactionStatus;
import com.bankofcli.enums.TransactionType;
import com.bankofcli.repository.AccountRepository;
import com.bankofcli.repository.TransactionRepository;
import com.bankofcli.service.AccountService;
import com.bankofcli.service.SystemLogService;
import com.bankofcli.service.TransactionService;
import com.bankofcli.util.ConnectionProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionServiceImpl implements TransactionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private static final BigDecimal MAX_TRANSACTION = new BigDecimal("1000000000.00");
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final SystemLogService systemLogService;
    private final ConnectionProvider connectionProvider;

    public TransactionServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, AccountService accountService, SystemLogService systemLogService, ConnectionProvider connectionProvider) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.accountService = Objects.requireNonNull(accountService);
        this.systemLogService = Objects.requireNonNull(systemLogService);
        this.connectionProvider = Objects.requireNonNull(connectionProvider);
    }

    @Override
    public Transaction deposit(long userId, long accountId, BigDecimal amount) {
        BigDecimal value = normalizeAmount(amount);
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Account account = lockedOwnedAccount(connection, userId, accountId);
                accountRepository.updateBalance(connection, accountId,
                        account.getBalance().add(value));
                Transaction transaction = completedTransaction(
                        null, accountId, value, TransactionType.DEPOSIT);
                transactionRepository.save(connection, transaction);
                connection.commit();
                systemLogService.info(userId, "Deposited " + value + " into account " + accountId);
                return transaction;
            } catch (RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } catch (SQLException exception) {
                rollback(connection, exception);
                throw new BankException("Deposit failed.", exception);
            }
        } catch (SQLException exception) {
            throw new BankException("Deposit failed.", exception);
        }
    }

    @Override
    public Transaction withdraw(long userId, long accountId, BigDecimal amount) {
        BigDecimal value = normalizeAmount(amount);
        boolean insufficientFunds = false;
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Account account = lockedOwnedAccount(connection, userId, accountId);
                if (account.getBalance().compareTo(value) < 0) {
                    insufficientFunds = true;
                    throw new BankException("Insufficient funds for this withdrawal.");
                }
                accountRepository.updateBalance(connection, accountId,
                        account.getBalance().subtract(value));
                Transaction transaction = completedTransaction(
                        accountId, null, value, TransactionType.WITHDRAWAL);
                transactionRepository.save(connection, transaction);
                connection.commit();
                systemLogService.info(userId, "Withdrew " + value + " from account " + accountId);
                return transaction;
            } catch (RuntimeException exception) {
                rollback(connection, exception);
                if (insufficientFunds) {
                    recordFailedTransaction(accountId, null, value, TransactionType.WITHDRAWAL);
                }
                systemLogService.error(userId, "Withdrawal failed for account " + accountId);
                throw exception;
            } catch (SQLException exception) {
                rollback(connection, exception);
                systemLogService.error(userId, "Withdrawal failed for account " + accountId);
                throw new BankException("Withdrawal failed.", exception);
            }
        } catch (SQLException exception) {
            throw new BankException("Withdrawal failed.", exception);
        }
    }

    @Override
    public Transaction transfer(long userId, long sourceAccountId,
                                long destinationAccountId, BigDecimal amount) {
        if (sourceAccountId == destinationAccountId) {
            throw new BankException("Source and destination accounts must be different.");
        }
        BigDecimal value = normalizeAmount(amount);
        boolean insufficientFunds = false;
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long firstId = Math.min(sourceAccountId, destinationAccountId);
                long secondId = Math.max(sourceAccountId, destinationAccountId);
                Account first = lockedAccount(connection, firstId);
                Account second = lockedAccount(connection, secondId);
                Account source = sourceAccountId == firstId ? first : second;
                Account destination = destinationAccountId == firstId ? first : second;

                ensureOwnedBy(source, userId);
                if (source.getBalance().compareTo(value) < 0) {
                    insufficientFunds = true;
                    throw new BankException("Insufficient funds for this transfer.");
                }

                accountRepository.updateBalance(connection, sourceAccountId,
                        source.getBalance().subtract(value));
                accountRepository.updateBalance(connection, destinationAccountId,
                        destination.getBalance().add(value));
                Transaction transaction = completedTransaction(
                        sourceAccountId, destinationAccountId, value, TransactionType.TRANSFER);
                transactionRepository.save(connection, transaction);
                connection.commit();
                systemLogService.info(userId, "Transferred " + value + " from account " + sourceAccountId
                        + " to account " + destinationAccountId);
                return transaction;
            } catch (RuntimeException exception) {
                rollback(connection, exception);
                if (insufficientFunds) {
                    recordFailedTransaction(sourceAccountId, destinationAccountId,
                            value, TransactionType.TRANSFER);
                }
                systemLogService.error(userId, "Transfer failed from account " + sourceAccountId
                        + " to account " + destinationAccountId);
                throw exception;
            } catch (SQLException exception) {
                rollback(connection, exception);
                systemLogService.error(userId, "Transfer failed from account " + sourceAccountId
                        + " to account " + destinationAccountId);
                throw new BankException("Transfer failed.", exception);
            }
        } catch (SQLException exception) {
            throw new BankException("Transfer failed.", exception);
        }
    }

    @Override
    public List<Transaction> getTransactionHistory(long userId, long accountId, int limit) {
        accountService.getOwnedAccount(userId, accountId);
        if (limit < 1 || limit > 500) {
            throw new BankException("History limit must be between 1 and 500.");
        }
        return transactionRepository.findByAccountId(accountId, limit);
    }

    private Account lockedOwnedAccount(Connection connection, long userId, long accountId) {
        Account account = lockedAccount(connection, accountId);
        ensureOwnedBy(account, userId);
        return account;
    }

    private Account lockedAccount(Connection connection, long accountId) {
        return accountRepository.findByIdForUpdate(connection, accountId)
                .orElseThrow(() -> new BankException("Account " + accountId + " was not found."));
    }

    private void ensureOwnedBy(Account account, long userId) {
        if (!Objects.equals(account.getUserId(), userId)) {
            throw new BankException("You are not authorized to access this account.");
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BankException("Amount is required.");
        }
        final BigDecimal value;
        try {
            value = amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new BankException("Amount may have at most two decimal places.");
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Amount must be greater than zero.");
        }
        if (value.compareTo(MAX_TRANSACTION) > 0) {
            throw new BankException("Amount exceeds the transaction limit.");
        }
        return value;
    }

    private Transaction completedTransaction(Long sourceAccountId, Long destinationAccountId,
                                             BigDecimal amount, TransactionType type) {
        return new Transaction(null, sourceAccountId, destinationAccountId, amount,
                LocalDateTime.now(), type, TransactionStatus.COMPLETED);
    }

    private void recordFailedTransaction(Long sourceAccountId, Long destinationAccountId,
                                         BigDecimal amount, TransactionType type) {
        try {
            transactionRepository.save(new Transaction(null, sourceAccountId,
                    destinationAccountId, amount, LocalDateTime.now(), type,
                    TransactionStatus.FAILED));
        } catch (BankException logFailure) {
            LOGGER.error("Could not persist failed transaction record", logFailure);
        }
    }

    private void rollback(Connection connection, Throwable original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            original.addSuppressed(rollbackFailure);
        }
    }
}
