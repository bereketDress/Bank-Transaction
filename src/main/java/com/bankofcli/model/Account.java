package com.bankofcli.model;

import com.bankofcli.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    private Long accountId;
    private String pinHash;
    private AccountType type;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private Long userId;

    public Account() {}

    public Account(Long accountId, String pinHash, AccountType type, BigDecimal balance,
                   LocalDateTime createdAt, Long userId) {
        this.accountId = accountId;
        this.pinHash = pinHash;
        this.type = type;
        this.balance = balance;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
