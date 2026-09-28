package com.bankofcli.repository.contract;

import com.bankofcli.model.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(long accountId);

    List<Account> findByUserId(long userId);

    void updateBalance(long accountId, BigDecimal balance);
}