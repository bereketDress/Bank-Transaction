package com.bankofcli.service.contract;

import com.bankofcli.model.Account;
import com.bankofcli.enums.AccountType;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    Account createAccount(long userId, String pin, AccountType accountType);
    Account loginAccount(long userId, long accountId, String pin);
    List<Account> getAccounts(long userId);
    BigDecimal getBalance(long userId, long accountId);
    Account getOwnedAccount(long userId, long accountId);
}
