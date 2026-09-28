package com.bankofcli.repository.contract;

import com.bankofcli.model.Transaction;

import java.util.List;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findByAccountId(long accountId, int limit);
}