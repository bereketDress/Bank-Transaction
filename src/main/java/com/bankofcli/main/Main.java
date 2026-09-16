package com.bankofcli.main;

import com.bankofcli.api.AccountApi;
import com.bankofcli.api.ConsoleInput;
import com.bankofcli.api.TransactionApi;
import com.bankofcli.api.UserApi;
import com.bankofcli.api.impl.AccountApiImpl;
import com.bankofcli.api.impl.TransactionApiImpl;
import com.bankofcli.api.impl.UserApiImpl;
import com.bankofcli.repository.AccountRepository;
import com.bankofcli.repository.SystemLogRepository;
import com.bankofcli.repository.TransactionRepository;
import com.bankofcli.repository.UserRepository;
import com.bankofcli.repository.impl.JdbcAccountRepository;
import com.bankofcli.repository.impl.JdbcSystemLogRepository;
import com.bankofcli.repository.impl.JdbcTransactionRepository;
import com.bankofcli.repository.impl.JdbcUserRepository;
import com.bankofcli.service.AccountService;
import com.bankofcli.service.SystemLogService;
import com.bankofcli.service.TransactionService;
import com.bankofcli.service.UserService;
import com.bankofcli.service.impl.AccountServiceImpl;
import com.bankofcli.service.impl.SystemLogServiceImpl;
import com.bankofcli.service.impl.TransactionServiceImpl;
import com.bankofcli.service.impl.UserServiceImpl;
import com.bankofcli.util.DBConnection;
import com.bankofcli.util.PasswordHasher;

import java.util.NoSuchElementException;
import java.util.Scanner;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            UserRepository users = new JdbcUserRepository();
            AccountRepository accounts = new JdbcAccountRepository();
            TransactionRepository transactions = new JdbcTransactionRepository();
            SystemLogRepository logs = new JdbcSystemLogRepository();
            PasswordHasher hasher = new PasswordHasher();

            SystemLogService logService = new SystemLogServiceImpl(logs);
            UserService userService = new UserServiceImpl(users, hasher, logService);
            AccountService accountService = new AccountServiceImpl(accounts, userService, hasher, logService);
            TransactionService transactionService = new TransactionServiceImpl(
                    accounts, transactions, accountService, logService, DBConnection::getConnection);

            ConsoleInput input = new ConsoleInput(scanner);
            TransactionApi transactionApi = new TransactionApiImpl(input, accountService, transactionService);
            AccountApi accountApi = new AccountApiImpl(input, accountService, transactionApi);
            UserApi userApi = new UserApiImpl(input, userService, accountApi);
            userApi.run();
        } catch (NoSuchElementException exception) {
            System.out.println("\nThank you for using bankofcli.");
        } catch (Exception exception) {
            System.err.println("bankofcli could not start. Check the database configuration.");
        } finally {
            DBConnection.closePool();
        }
    }
}

