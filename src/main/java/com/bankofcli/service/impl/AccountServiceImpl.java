package com.bankofcli.service.impl;

import com.bankofcli.exception.BankException;
import com.bankofcli.model.Account;
import com.bankofcli.enums.AccountType;
import com.bankofcli.repository.AccountRepository;
import com.bankofcli.service.AccountService;
import com.bankofcli.service.SystemLogService;
import com.bankofcli.service.UserService;
import com.bankofcli.util.PasswordHasher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class AccountServiceImpl implements AccountService {
    private static final Pattern PIN_PATTERN = Pattern.compile("\\d{4,6}");
    private final AccountRepository accountRepository;
    private final UserService userService;
    private final PasswordHasher passwordHasher;
    private final SystemLogService systemLogService;

    public AccountServiceImpl(AccountRepository accountRepository, UserService userService, PasswordHasher passwordHasher, SystemLogService systemLogService) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.userService = Objects.requireNonNull(userService);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.systemLogService = Objects.requireNonNull(systemLogService);
    }

    @Override
    public Account createAccount(long userId, String pin, AccountType accountType) {
        userService.getUser(userId);
        validatePin(pin);
        if (accountType == null) {
            throw new BankException("Account type is required.");
        }
        Account account = new Account(null, passwordHasher.hash(pin), accountType,
                new BigDecimal("0.00"), LocalDateTime.now(), userId);
        Account saved = accountRepository.save(account);
        systemLogService.info(userId, "Created " + accountType + " account " + saved.getAccountId());
        return saved;
    }

    @Override
    public Account loginAccount(long userId, long accountId, String pin) {
        Account account = getOwnedAccount(userId, accountId);
        if (!passwordHasher.matches(pin, account.getPinHash())) {
            systemLogService.error(userId, "Failed account login attempt for account " + accountId);
            throw new BankException("Incorrect account ID or PIN.");
        }
        systemLogService.info(userId, "Account " + accountId + " authenticated successfully");
        return account;
    }

    @Override
    public List<Account> getAccounts(long userId) {
        userService.getUser(userId);
        return accountRepository.findByUserId(userId);
    }

    @Override
    public BigDecimal getBalance(long userId, long accountId) {
        return getOwnedAccount(userId, accountId).getBalance();
    }

    @Override
    public Account getOwnedAccount(long userId, long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankException("Account " + accountId + " was not found."));
        ensureOwnedBy(account, userId);
        return account;
    }

    private void ensureOwnedBy(Account account, long userId) {
        if (!Objects.equals(account.getUserId(), userId)) {
            throw new BankException("You are not authorized to access this account.");
        }
    }

    private void validatePin(String pin) {
        if (pin == null || !PIN_PATTERN.matcher(pin).matches()) {
            throw new BankException("PIN must contain 4 to 6 digits.");
        }
    }
}
