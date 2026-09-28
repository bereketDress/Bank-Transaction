package com.bankofcli.ui.swing;

import com.bankofcli.model.User;
import com.bankofcli.service.contract.AccountService;
import com.bankofcli.service.contract.TransactionService;

import javax.swing.*;
import java.math.BigDecimal;

public class TransactionsScreen {

    private final AccountService accounts;
    private final TransactionService transactions;
    private final UiSupport ui;

    public TransactionsScreen(
            AccountService accounts,
            TransactionService transactions,
            UiSupport ui) {

        this.accounts = accounts;
        this.transactions = transactions;
        this.ui = ui;
    }

    public void show(
            User user,
            long accountId,
            Runnable onBack) {

        ui.button(
                "Balance",
                () -> ui.run(
                        () -> accounts.getBalance(
                                user.getUserId(),
                                accountId
                        ),
                        balance ->
                                ui.display(
                                        "Balance",
                                        "$" + balance
                                )
                )
        );

        ui.button(
                "Deposit",
                () -> moveMoney(
                        user,
                        accountId,
                        "Deposit"
                )
        );

        ui.button(
                "Withdraw",
                () -> moveMoney(
                        user,
                        accountId,
                        "Withdraw"
                )
        );

        ui.button(
                "Transfer",
                () -> moveMoney(
                        user,
                        accountId,
                        "Transfer"
                )
        );

        ui.button(
                "Transaction history",
                () -> ui.run(
                        () -> transactions
                                .getTransactionHistory(
                                        user.getUserId(),
                                        accountId,
                                        20
                                ),

                        list -> {

                            StringBuilder text =
                                    new StringBuilder();

                            list.forEach(tx ->
                                    text.append(
                                                    tx.getTransactionId()
                                            )
                                            .append(" | ")
                                            .append(
                                                    tx.getTransactionType()
                                            )
                                            .append(" | $")
                                            .append(
                                                    tx.getAmount()
                                            )
                                            .append(" | ")
                                            .append(
                                                    tx.getTransactionStatus()
                                            )
                                            .append('\n')
                            );

                            ui.display(
                                    "Latest 20 transactions",
                                    text.isEmpty()
                                            ? "No transactions."
                                            : text.toString()
                            );
                        }
                )
        );

        ui.button(
                "Back to accounts",
                onBack
        );
    }

    private void moveMoney(
            User user,
            long accountId,
            String operation) {

        JTextField amount =
                new JTextField();

        JTextField destination =
                new JTextField();

        boolean transfer =
                operation.equals("Transfer");

        Object[] fields = transfer
                ? new Object[]{
                "Destination account ID",
                destination,
                "Amount",
                amount
        }
                : new Object[]{
                "Amount",
                amount
        };

        if (!ui.confirm(operation, fields)) {
            return;
        }

        BigDecimal value =
                new BigDecimal(
                        amount.getText().trim()
                );

        long destinationId =
                transfer
                        ? Long.parseLong(
                        destination
                                .getText()
                                .trim()
                )
                        : 0;

        ui.run(
                () -> switch (operation) {

                    case "Deposit" ->
                            transactions.deposit(
                                    user.getUserId(),
                                    accountId,
                                    value
                            );

                    case "Withdraw" ->
                            transactions.withdraw(
                                    user.getUserId(),
                                    accountId,
                                    value
                            );

                    default ->
                            transactions.transfer(
                                    user.getUserId(),
                                    accountId,
                                    destinationId,
                                    value
                            );
                },

                transaction ->
                        ui.display(
                                operation,
                                "Completed. Transaction ID: "
                                        + transaction
                                        .getTransactionId()
                        )
        );
    }
}
