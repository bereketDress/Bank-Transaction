package com.bankofcli.api.impl;

import com.bankofcli.api.ConsoleInput;
import com.bankofcli.api.TransactionApi;
import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;
import com.bankofcli.service.AccountService;
import com.bankofcli.service.TransactionService;
import java.util.List;

public final class TransactionApiImpl implements TransactionApi {
    private final ConsoleInput input;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public TransactionApiImpl(ConsoleInput input, AccountService accountService, TransactionService transactionService) {
        this.input = input;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Override
    public void showMenu(long userId, long accountId) {
        boolean active = true;
        while (active) {
            System.out.printf("""

                    ===== ACCOUNT %d =====
                    1. View balance
                    2. Deposit
                    3. Withdraw
                    4. Transfer
                    5. Transaction history
                    6. Back
                    =======================
                    """, accountId);
            try {
                switch (input.readInt("Choose an option: ")) {
                    case 1 -> showBalance(userId, accountId);
                    case 2 -> deposit(userId, accountId);
                    case 3 -> withdraw(userId, accountId);
                    case 4 -> transfer(userId, accountId);
                    case 5 -> showHistory(userId, accountId);
                    case 6 -> active = false;
                    default -> System.out.println("Please choose 1 to 6.");
                }
            } catch (RuntimeException exception) {
                input.showError(exception);
            }
        }
    }

    private void showBalance(long userId, long accountId) {
        System.out.println("Current balance: $" + accountService.getBalance(userId, accountId));
    }

    private void deposit(long userId, long accountId) {
        Transaction transaction = transactionService.deposit(userId, accountId, input.readAmount());
        System.out.println("Deposit completed. Transaction #" + transaction.getTransactionId());
    }

    private void withdraw(long userId, long accountId) {
        Transaction transaction = transactionService.withdraw(userId, accountId, input.readAmount());
        System.out.println("Withdrawal completed. Transaction #" + transaction.getTransactionId());
    }

    private void transfer(long userId, long sourceAccountId) {
        long destinationId = input.readLong("Destination account ID: ");
        Transaction transaction = transactionService.transfer(
                userId, sourceAccountId, destinationId, input.readAmount());
        System.out.println("Transfer completed. Transaction #" + transaction.getTransactionId());
    }

    private void showHistory(long userId, long accountId) {
        List<Transaction> history = transactionService.getTransactionHistory(userId, accountId, 20);
        if (history.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        System.out.println("ID | Date | Type | Amount | Status | From -> To");
        for (Transaction transaction : history) {
            System.out.printf("%d | %s | %s | $%s | %s | %s -> %s%n",
                    transaction.getTransactionId(), transaction.getExecutedAt(),
                    transaction.getType(), transaction.getAmount(), transaction.getStatus(),
                    displayId(transaction.getSourceAccountId()),
                    displayId(transaction.getDestinationAccountId()));
        }
    }

    private String displayId(Long accountId) {
        return accountId == null ? "external" : accountId.toString();
    }
}
