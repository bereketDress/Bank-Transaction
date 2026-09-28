package com.bankofcli.model;

import com.bankofcli.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
/**
 * Objects.requireNonNull(value)
 * -> checks that value is not null.
 * -> if value is null, it throws NullPointerException.
 *
 * Object
 * -> java.lang.Object is the top-level parent class of Java classes.
 *
 * Objects
 * -> java.util.Objects is a utility class.
 * -> contains methods such as equals(), requireNonNull(), hash(), etc.
 *
 * final
 * -> the variable reference cannot be reassigned to another object.
 * -> for example, a final ArrayList can still add/remove elements.
 *
 * BigDecimal
 * -> class used for precise decimal calculations.
 * -> commonly used for money.
 * -> avoids many floating-point precision problems that can happen with double.
 */
public class Account {

    private Long accountId;
    private String pinHash;
    private AccountType type;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private User user;

    private final List<Transaction> outgoingTransactions = new ArrayList<>();
    private final List<Transaction> incomingTransactions = new ArrayList<>();


    public Account(Long accountId, String pinHash, AccountType type, BigDecimal balance, LocalDateTime createdAt, User user) {
        this.accountId = accountId;
        this.pinHash = Objects.requireNonNull(pinHash);
        this.type = Objects.requireNonNull(type);
        this.balance = balance == null ? BigDecimal.ZERO : balance;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.user = user;
    }

    public void deposit(BigDecimal amount) {
        validateAmount(amount);
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        validateAmount(amount);

        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        balance = balance.subtract(amount);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    public void addOutgoingTransaction(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");
        outgoingTransactions.add(transaction);
    }

    public void addIncomingTransaction(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");
        incomingTransactions.add(transaction);
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
        this.pinHash = Objects.requireNonNull(pinHash);
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = Objects.requireNonNull(type);
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Transaction> getOutgoingTransactions() {
        return Collections.unmodifiableList(outgoingTransactions);
    }

    public List<Transaction> getIncomingTransactions() {
        return Collections.unmodifiableList(incomingTransactions);
    }
}