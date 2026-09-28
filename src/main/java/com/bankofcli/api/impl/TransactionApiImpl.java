package com.bankofcli.api.impl;

import com.bankofcli.api.reader.ConsoleInput;
import com.bankofcli.api.contract.TransactionApi;
import com.bankofcli.model.Transaction;
import com.bankofcli.service.contract.AccountService;
import com.bankofcli.service.contract.TransactionService;

import java.util.List;

public class TransactionApiImpl implements TransactionApi {

    private final ConsoleInput input;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public TransactionApiImpl(ConsoleInput input, AccountService accountService,TransactionService transactionService) {

        this.input = input;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Override
    public void showMenu(long userId, long accountId) {

        while (true) {

            System.out.println("""
                    1. Balance
                    2. Deposit
                    3. Withdraw
                    4. Transfer
                    5. History
                    6. Back
                    """);

            int choice = Integer.parseInt(input.readText("Choose: "));

            switch (choice) {
                case 1 -> showBalance(userId, accountId);
                case 2 -> deposit(userId, accountId);
                case 3 -> withdraw(userId, accountId);
                case 4 -> transfer(userId, accountId);
                case 5 -> showHistory(userId, accountId);
                case 6 -> {
                    return;
                }
                default -> System.out.println("Invalid option");
            }
        }
    }

    private void showBalance(long userId, long accountId) {
        System.out.println("Balance: $" + accountService.getBalance(userId, accountId));
    }

    private void deposit(long userId, long accountId) {

        Transaction transaction = transactionService.deposit(userId, accountId, input.readAmount());

        System.out.println("Deposit completed $" + transaction.getAmount()
        );
    }

    private void withdraw(long userId, long accountId) {

        Transaction transaction = transactionService.withdraw(userId, accountId, input.readAmount());

        System.out.println("Withdrawal completed #" + transaction.getTransactionId());
    }

    private void transfer(long userId, long accountId) {

        long destinationId =
                input.readLong("Destination account ID: ");

        Transaction transaction = transactionService.transfer(
                userId,
                accountId,
                destinationId,
                input.readAmount()
        );

        System.out.println("Transfer completed $" + transaction.getAmount());
    }

    private void showHistory(long userId, long accountId) {

        List<Transaction> history = transactionService.getTransactionHistory(
                        userId,
                        accountId,
                        20
                );

        if (history.isEmpty()) {
            System.out.println("No transactions");
            return;
        }

        for (Transaction transaction : history) {

            System.out.println(
                    transaction.getTransactionId() + " | " +
                            transaction.getTransactionType() + " | $" +
                            transaction.getAmount() + " | " +
                            transaction.getTransactionStatus()
            );
        }
    }
}