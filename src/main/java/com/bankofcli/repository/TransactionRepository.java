package com.bankofcli.repository;

import com.bankofcli.model.Transaction;

import java.sql.Connection;
import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);

    Transaction save(Connection connection, Transaction transaction);

    List<Transaction> findByAccountId(long accountId, int limit);
}
