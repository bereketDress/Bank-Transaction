package com.bankofcli.ui;

import com.bankofcli.repository.impl.AccountRepositoryImpl;
import com.bankofcli.repository.impl.TransactionRepositoryImpl;
import com.bankofcli.repository.impl.UserRepositoryImpl;

import com.bankofcli.service.impl.AccountServiceImpl;
import com.bankofcli.service.impl.TransactionServiceImpl;
import com.bankofcli.service.impl.UserServiceImpl;

import com.bankofcli.ui.swing.BankWindow;
import com.bankofcli.util.DBConnection;
import com.bankofcli.util.PasswordHasher;

import javax.swing.SwingUtilities;

public class MainTest {

    public static void main(String[] args) {

        /*
        close database connection pool when the Java application shuts down
        Swing GUI: those code runs on the Event Dispatch Thread (EDT)
         */

        Runtime.getRuntime().addShutdownHook(new Thread(DBConnection::closePool));
        SwingUtilities.invokeLater(() -> {

            UserRepositoryImpl users = new UserRepositoryImpl();
            AccountRepositoryImpl accounts = new AccountRepositoryImpl(users);
            TransactionRepositoryImpl transactions = new TransactionRepositoryImpl(accounts);

            PasswordHasher hasher = new PasswordHasher();

            UserServiceImpl userService = new UserServiceImpl(users, hasher);
            AccountServiceImpl accountService = new AccountServiceImpl(accounts, userService, hasher);
            TransactionServiceImpl transactionService = new TransactionServiceImpl(accounts, transactions, accountService);

            BankWindow window = new BankWindow(
                            userService,
                            accountService,
                            transactionService
                    );

            window.setVisible(true);
        });
    }
}