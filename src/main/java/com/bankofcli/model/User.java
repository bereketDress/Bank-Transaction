package com.bankofcli.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {

    private Long userId;
    private String userName;
    private String userEmail;
    private String passwordHash;

    private final List<Account> accounts = new ArrayList<>();
    private final List<SystemLog> systemLogs = new ArrayList<>();

    public User(Long userId, String userName, String userEmail, String passwordHash) {
        this.userId = userId;
        this.userName = Objects.requireNonNull(userName);
        this.userEmail = Objects.requireNonNull(userEmail);
        this.passwordHash = Objects.requireNonNull(passwordHash);
    }

    public void addAccount(Account account) {
        Objects.requireNonNull(account);

        if (!accounts.contains(account)) {
            accounts.add(account);
            account.setUser(this);
        }
    }

    public void addSystemLog(SystemLog systemLog) {
        Objects.requireNonNull(systemLog);
        systemLogs.add(systemLog);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = Objects.requireNonNull(userName);
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = Objects.requireNonNull(userEmail);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<SystemLog> getSystemLogs() {
        return systemLogs;
    }
}