package com.bankofcli.api.impl;

import com.bankofcli.api.AccountApi;
import com.bankofcli.api.ConsoleInput;
import com.bankofcli.api.TransactionApi;
import com.bankofcli.model.Account;
import com.bankofcli.model.User;
import com.bankofcli.enums.AccountType;
import com.bankofcli.service.AccountService;
import java.util.List;
import java.util.Locale;

public final class AccountApiImpl implements AccountApi {
    private final ConsoleInput input;
    private final AccountService accountService;
    private final TransactionApi transactionApi;

    public AccountApiImpl(ConsoleInput input, AccountService accountService, TransactionApi transactionApi) {
        this.input = input;
        this.accountService = accountService;
        this.transactionApi = transactionApi;
    }

    @Override
    public void showMenu(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("""

                    ===== USER MENU =====
                    1. List accounts
                    2. Open account
                    3. Access account
                    4. Logout
                    =====================
                    """);
            switch (input.readInt("Choose an option: ")) {
                case 1 -> listAccounts(user.getUserId());
                case 2 -> createAccount(user.getUserId());
                case 3 -> accessAccount(user.getUserId());
                case 4 -> loggedIn = false;
                default -> System.out.println("Please choose 1 to 4.");
            }
        }
    }

    private void listAccounts(long userId) {
        try {
            List<Account> accounts = accountService.getAccounts(userId);
            if (accounts.isEmpty()) {
                System.out.println("You do not have any accounts yet.");
                return;
            }
            System.out.println("Account ID | Type     | Balance");
            for (Account account : accounts) {
                System.out.printf("%-10d | %-8s | $%s%n", account.getAccountId(),
                        account.getType(), account.getBalance());
            }
        } catch (RuntimeException exception) {
            input.showError(exception);
        }
    }

    private void createAccount(long userId) {
        try {
            AccountType type = AccountType.valueOf(
                    input.readText("Account type (CHECKING/SAVING): ").toUpperCase(Locale.ROOT));
            String pin = input.readText("Choose a 4-6 digit PIN: ");
            Account account = accountService.createAccount(userId, pin, type);
            System.out.println("Account created. Your account ID is " + account.getAccountId() + ".");
        } catch (RuntimeException exception) {
            input.showError(exception);
        }
    }

    private void accessAccount(long userId) {
        try {
            long accountId = input.readLong("Account ID: ");
            String pin = input.readText("PIN: ");
            Account account = accountService.loginAccount(userId, accountId, pin);
            transactionApi.showMenu(userId, account.getAccountId());
        } catch (RuntimeException exception) {
            input.showError(exception);
        }
    }
}
