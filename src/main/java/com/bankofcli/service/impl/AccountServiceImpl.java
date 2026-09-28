package com.bankofcli.service.impl;

import com.bankofcli.enums.AccountType;
import com.bankofcli.exception.BankException;
import com.bankofcli.model.Account;
import com.bankofcli.model.User;
import com.bankofcli.repository.contract.AccountRepository;
import com.bankofcli.service.contract.AccountService;
import com.bankofcli.service.contract.UserService;
import com.bankofcli.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class AccountServiceImpl implements AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final UserService userService;
    private final PasswordHasher passwordHasher;

    public AccountServiceImpl(AccountRepository accountRepository, UserService userService, PasswordHasher passwordHasher) {
        this.accountRepository = accountRepository;
        this.userService = userService;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Account createAccount(long userId, String pin, AccountType accountType) {
        User user = userService.getUser(userId);

        validatePin(pin);

        if (accountType == null) {
            throw new BankException("Account type is required");
        }

        Account account = new Account(
                null,
                passwordHasher.hash(pin),
                accountType,
                BigDecimal.ZERO,
                LocalDateTime.now(),
                user
        );

        Account savedAccount = accountRepository.save(account);
        logger.info("{} account created successfully", accountType);
        return savedAccount;
    }

    @Override
    public Account loginAccount(long userId, long accountId, String pin) {
        Account account = getOwnedAccount(userId, accountId);

        if (!passwordHasher.matches(pin, account.getPinHash())) {
            throw new BankException("Invalid PIN");
        }

        logger.info("Account login successful");
        return account;
    }

    @Override
    public List<Account> getAccounts(long userId) {
        userService.getUser(userId);//check user exist
        return accountRepository.findByUserId(userId);//gets all accounts belonging to user
    }

    @Override
    public BigDecimal getBalance(long userId, long accountId) {
        return getOwnedAccount(userId, accountId).getBalance();
    }

    @Override
    public Account getOwnedAccount(long userId, long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankException("Account not found"));

        if (!Objects.equals(account.getUser().getUserId(), userId)) {
            throw new BankException("Account does not belong to this user");
        }

        return account;
    }

    private void validatePin(String pin) {
        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new BankException("PIN must be 4 to 6 digits");
        }
    }
}
