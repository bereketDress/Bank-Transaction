package com.bankofcli.api.impl;

import com.bankofcli.api.AccountApi;
import com.bankofcli.api.ConsoleInput;
import com.bankofcli.api.UserApi;
import com.bankofcli.model.User;
import com.bankofcli.service.UserService;

public final class UserApiImpl implements UserApi {
    private final ConsoleInput input;
    private final UserService userService;
    private final AccountApi accountApi;

    public UserApiImpl(ConsoleInput input, UserService userService, AccountApi accountApi) {
        this.input = input;
        this.userService = userService;
        this.accountApi = accountApi;
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            printWelcomeMenu();
            switch (input.readInt("Choose an option: ")) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> running = false;
                default -> System.out.println("Please choose 1, 2, or 3.");
            }
        }
        System.out.println("Thank you for using bankofcli.");
    }

    private void register() {
        try {
            String name = input.readText("Name: ");
            String email = input.readText("Email: ");
            String password = input.readLine("Password (minimum 8 characters): ");
            User user = userService.registerUser(name, email, password);
            System.out.println("Registration successful. Your user ID is " + user.getUserId() + ".");
        } catch (RuntimeException exception) {
            input.showError(exception);
        }
    }

    private void login() {
        try {
            User user = userService.loginUser(input.readText("Email: "), input.readLine("Password: "));
            System.out.println("Welcome, " + user.getUserName() + "!");
            accountApi.showMenu(user);
        } catch (RuntimeException exception) {
            input.showError(exception);
        }
    }

    private void printWelcomeMenu() {
        System.out.println("""

                ===== bankofcli =====
                1. Register
                2. Login
                3. Exit
                =======================
                """);
    }
}
