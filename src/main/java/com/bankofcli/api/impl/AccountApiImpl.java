package com.bankofcli.api.impl;

import com.bankofcli.api.contract.AccountApi;
import com.bankofcli.api.reader.ConsoleInput;
import com.bankofcli.api.contract.TransactionApi;
import com.bankofcli.enums.AccountType;
import com.bankofcli.model.Account;
import com.bankofcli.model.User;
import com.bankofcli.service.contract.AccountService;

import java.util.List;

public class AccountApiImpl implements AccountApi {

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

        while (true) {

            System.out.println("""
                    1. List accounts
                    2. Create account
                    3. Access account
                    4. Logout
                    """);

            int choice = Integer.parseInt(input.readText("Choose: "));

            switch (choice) {
                case 1 -> listAccounts(user.getUserId());
                case 2 -> createAccount(user.getUserId());
                case 3 -> accessAccount(user.getUserId());
                case 4 -> {
                    return;
                }
                default -> System.out.println("Invalid option");
            }
        }
    }

    private void listAccounts(long userId) {

        List<Account> accounts = accountService.getAccounts(userId);

        if (accounts.isEmpty()) {
            System.out.println("No accounts");
            return;
        }

        for (Account account : accounts) {
            System.out.println(
                    account.getAccountId() + " | " +
                            account.getType() + " | $" +
                            account.getBalance()
            );
        }
    }

    private void createAccount(long userId) {

        String typeInput = input.readText("Type (CHECKING/SAVING): ");

        AccountType type = AccountType.valueOf(typeInput.toUpperCase());

        String pin = input.readText("PIN: ");

        Account account = accountService.createAccount(userId, pin, type);

        System.out.println("Account created. ID: " + account.getAccountId());
    }

    private void accessAccount(long userId) {

        long accountId = input.readLong("Account ID: ");

        String pin = input.readText("PIN: ");

        Account account = accountService.loginAccount(userId, accountId, pin);

        transactionApi.showMenu(userId, account.getAccountId()
        );
    }
}