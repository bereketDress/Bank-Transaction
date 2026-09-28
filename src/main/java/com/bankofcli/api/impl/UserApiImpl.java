package com.bankofcli.api.impl;

import com.bankofcli.api.contract.AccountApi;
import com.bankofcli.api.reader.ConsoleInput;
import com.bankofcli.api.contract.UserApi;
import com.bankofcli.model.User;
import com.bankofcli.service.contract.UserService;

public class UserApiImpl implements UserApi {

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

        while (true) {

            System.out.println("""
                    1. Register
                    2. Login
                    3. Exit
                    """);

            int choice = Integer.parseInt(input.readText("Choose: "));

            switch (choice) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option");
            }
        }
    }

    private void register() {

        String name = input.readText("Name: ");
        String email = input.readText("Email: ");
        String password = input.readText("Password: ");

        User user = userService.registerUser(name, email, password);

        System.out.println("Registered. User ID: " + user.getUserId());
    }

    private void login() {

        String email = input.readText("Email: ");
        String password = input.readText("Password: ");

        User user = userService.loginUser(email, password);

        System.out.println("Welcome " + user.getUserName());

        accountApi.showMenu(user);
    }
}