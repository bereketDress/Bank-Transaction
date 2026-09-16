package com.bankofcli.service;

import com.bankofcli.model.Transaction;
import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    Transaction deposit(long userId, long accountId, BigDecimal amount);
    Transaction withdraw(long userId, long accountId, BigDecimal amount);
    Transaction transfer(long userId, long sourceAccountId, long destinationAccountId, BigDecimal amount);
    List<Transaction> getTransactionHistory(long userId, long accountId, int limit);
}
