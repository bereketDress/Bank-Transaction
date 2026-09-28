package com.bankofcli.main;

import com.bankofcli.model.User;
import com.bankofcli.service.contract.UserService;
import javax.swing.*;
import java.util.function.Consumer;

// Login/register screen; returns the authenticated User via callback.
public class LoginScreen {
    private final UserService users;
    private final UiSupport ui;

    public LoginScreen(UserService users, UiSupport ui) { this.users = users; this.ui = ui; }

    public void show(Consumer<User> onSuccess) {
        ui.button("Login", () -> authenticate(false, onSuccess));
        ui.button("Register", () -> authenticate(true, onSuccess));
        ui.button("Exit", () -> System.exit(0));
    }

    private void authenticate(boolean register, Consumer<User> onSuccess) {
        JTextField name = new JTextField(), email = new JTextField();
        JPasswordField password = new JPasswordField();
        Object[] fields = register
                ? new Object[]{"Name", name, "Email", email, "Password", password}
                : new Object[]{"Email", email, "Password", password};
        if (!ui.confirm(register ? "Register" : "Login", fields)) return;

        String secret = ui.secret(password), n = name.getText().trim(), e = email.getText().trim();
        ui.run(() -> register ? users.registerUser(n, e, secret) : users.loginUser(e, secret), onSuccess);
    }
}