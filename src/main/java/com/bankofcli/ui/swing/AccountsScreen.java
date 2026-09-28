package com.bankofcli.ui.swing;

import com.bankofcli.enums.AccountType;
import com.bankofcli.model.Account;
import com.bankofcli.model.User;
import com.bankofcli.service.contract.AccountService;

import javax.swing.*;
import java.util.function.Consumer;
/**
 * AccountsScreen handles account-related actions:
 * list accounts, create account, access account, and logout.
 */

public class AccountsScreen {
    private final AccountService accounts;
    private final UiSupport ui;

    public AccountsScreen(AccountService accounts, UiSupport ui) { this.accounts = accounts; this.ui = ui; }

    public void show(User user, Consumer<Account> onSelected, Runnable onLogout) {
        ui.button("List accounts", () -> ui.run(
                () -> accounts.getAccounts(user.getUserId()),
                list -> {
                    StringBuilder t = new StringBuilder();
                    list.forEach(a -> t.append(a.getAccountId()).append(" | ").append(a.getType())
                            .append(" | $").append(a.getBalance()).append('\n'));
                    ui.display("Accounts", t.isEmpty() ? "No accounts yet." : t.toString());
                }));

        ui.button("Create account", () -> {
            JComboBox<AccountType> type = new JComboBox<>(AccountType.values());
            JPasswordField pin = new JPasswordField();
            if (!ui.confirm("Create account", new Object[]{"Type", type, "PIN", pin})) return;
            String p = ui.secret(pin);
            AccountType t = (AccountType) type.getSelectedItem();
            ui.run(() -> accounts.createAccount(user.getUserId(), p, t),
                    a -> ui.display("Account created", "Account ID: " + a.getAccountId()));
        });

        ui.button("Access account", () -> {
            JTextField id = new JTextField();
            JPasswordField pin = new JPasswordField();
            if (!ui.confirm("Access account", new Object[]{"Account ID", id, "PIN", pin})) return;
            long selectedId = Long.parseLong(id.getText().trim());
            String p = ui.secret(pin);
            ui.run(() -> accounts.loginAccount(user.getUserId(), selectedId, p), onSelected);
        });

        ui.button("Logout", onLogout);
    }
}