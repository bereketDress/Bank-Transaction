package com.bankofcli.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private Long userId;
    private String userName;
    private String userEmail;
    private String passwordHash;
    private List<Account> accounts = new ArrayList<>();
    private List<SystemLog> systemLogs = new ArrayList<>();

    public User() {
    }

    public User(Long userId, String userName, String userEmail, String passwordHash) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.passwordHash = passwordHash;
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
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = new ArrayList<>(Objects.requireNonNull(accounts));
    }

    public List<SystemLog> getSystemLogs() {
        return systemLogs;
    }

    public void setSystemLogs(List<SystemLog> systemLogs) {
        this.systemLogs = new ArrayList<>(Objects.requireNonNull(systemLogs));
    }
}
