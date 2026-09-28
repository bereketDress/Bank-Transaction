package com.bankofcli.service.impl;

import com.bankofcli.enums.TransactionStatus;
import com.bankofcli.enums.TransactionType;
import com.bankofcli.exception.BankException;
import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;
import com.bankofcli.repository.contract.AccountRepository;
import com.bankofcli.repository.contract.TransactionRepository;
import com.bankofcli.service.contract.AccountService;
import com.bankofcli.service.contract.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;
    private final AccountService accountService;

    public TransactionServiceImpl(
            AccountRepository accountRepo,
            TransactionRepository transactionRepo,
            AccountService accountService) {

        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
        this.accountService = accountService;
    }

    @Override
    public Transaction deposit(long userId, long accountId, BigDecimal amount) {

        Account account = accountService.getOwnedAccount(userId, accountId);

        account.deposit(amount);

        accountRepo.updateBalance(accountId, account.getBalance());

        return save(amount, TransactionType.DEPOSIT, null, account);
    }

    @Override
    public Transaction withdraw(long userId, long accountId, BigDecimal amount) {

        Account account = accountService.getOwnedAccount(userId, accountId);

        account.withdraw(amount);

        accountRepo.updateBalance(accountId,account.getBalance());

        return save(amount, TransactionType.WITHDRAWAL, account, null);
    }

    @Override
    public Transaction transfer(long userId, long sourceId, long destinationId, BigDecimal amount) {

        if (sourceId == destinationId) {
            throw new BankException("Accounts must be different");
        }

        Account source = accountService.getOwnedAccount(userId, sourceId);

        Account destination = accountRepo.findById(destinationId)
                        .orElseThrow(() -> new BankException("Account not found"));


        source.withdraw(amount);
        destination.deposit(amount);

        accountRepo.updateBalance(sourceId, source.getBalance());

        accountRepo.updateBalance(destinationId, destination.getBalance());

        return save(amount, TransactionType.TRANSFER, source, destination);
    }

    @Override
    public List<Transaction> getTransactionHistory(long userId, long accountId, int limit) {

        accountService.getOwnedAccount(userId, accountId);

        return transactionRepo.findByAccountId(accountId, limit);
    }

    private Transaction save(
            BigDecimal amount,
            TransactionType type,
            Account source,
            Account destination) {

        Transaction transaction = new Transaction(
                null,
                amount,
                LocalDateTime.now(),
                type,
                TransactionStatus.COMPLETED,
                source,
                destination
        );

        Transaction savedTransaction = transactionRepo.save(transaction);
        logger.info("{} transaction completed successfully", type);
        return savedTransaction;
    }
}
