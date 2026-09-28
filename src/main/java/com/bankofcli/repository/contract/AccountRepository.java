package com.bankofcli.repository;

import com.bankofcli.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findById(long accountId);

    Optional<Account> findByIdForUpdate(Connection connection, long accountId);

    List<Account> findByUserId(long userId);

    void updateBalance(Connection connection, long accountId, BigDecimal newBalance);
}
