package com.bankofcli.ui.swing;

import com.bankofcli.model.Account;
import com.bankofcli.model.User;
import com.bankofcli.service.contract.*;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
/**
 * Main window of the banking application.
 * It switches between:
 * Login screen
 * Accounts screen
 * Transactions screen
 */
public class BankWindow extends JFrame {

    private final JPanel menu = new JPanel();

    private final JLabel heading =
            new JLabel(
                    "Welcome to Bank of CLI",
                    SwingConstants.CENTER
            );

    private final UiSupport ui;

    private final LoginScreen login;
    private final AccountsScreen accountsScreen;
    private final TransactionsScreen transactionsScreen;

    private User user;

    public BankWindow(
            UserService users,
            AccountService accounts,
            TransactionService transactions) {

        super("Bank of CLI");

        this.ui = new UiSupport(this, menu);

        this.login =
                new LoginScreen(users, ui);

        this.accountsScreen =
                new AccountsScreen(accounts, ui);

        this.transactionsScreen =
                new TransactionsScreen(
                        accounts,
                        transactions,
                        ui
                );

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setResizable(false);
        setLocationRelativeTo(null);

        URL imageUrl =
                getClass().getResource(
                        "/images/bank-background.png"
                );

        if (imageUrl == null) {
            throw new RuntimeException(
                    "Background image not found"
            );
        }

        Image image =
                new ImageIcon(imageUrl)
                        .getImage()
                        .getScaledInstance(
                                800,
                                500,
                                Image.SCALE_SMOOTH
                        );

        JLabel background =
                new JLabel(new ImageIcon(image));

        background.setLayout(
                new BorderLayout()
        );

        heading.setFont(
                new Font(
                        "Serif",
                        Font.BOLD,
                        28
                )
        );

        heading.setForeground(
                new Color(255, 210, 70)
        );

        heading.setBorder(
                BorderFactory.createEmptyBorder(
                        35,
                        0,
                        20,
                        0
                )
        );

        menu.setOpaque(false);

        menu.setLayout(
                new BoxLayout(
                        menu,
                        BoxLayout.Y_AXIS
                )
        );

        menu.setBorder(
                BorderFactory.createEmptyBorder(
                        35,
                        290,
                        70,
                        290
                )
        );

        background.add(
                heading,
                BorderLayout.NORTH
        );

        background.add(
                menu,
                BorderLayout.CENTER
        );

        setContentPane(background);

        showLogin();
    }

    private void reset(String title) {

        heading.setText(title);

        menu.removeAll();

        menu.revalidate();

        menu.repaint();
    }

    private void showLogin() {

        user = null;

        reset("Welcome to Bank of CLI");

        login.show(u -> {

            user = u;

            showAccounts();
        });
    }

    private void showAccounts() {

        reset(
                "Welcome, " +
                        user.getUserName()
        );

        accountsScreen.show(
                user,
                this::showTransactions,
                this::showLogin
        );
    }

    private void showTransactions(Account account) {

        reset(
                "Account " +
                        account.getAccountId()
        );

        transactionsScreen.show(
                user,
                account.getAccountId(),
                this::showAccounts
        );
    }
}