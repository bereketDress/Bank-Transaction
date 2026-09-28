package com.bankofcli.model;

import com.bankofcli.enums.TransactionStatus;
import com.bankofcli.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {

    private Long transactionId;
    private BigDecimal amount;
    private LocalDateTime executedAt;

    private TransactionType transactionType;
    private TransactionStatus transactionStatus;

    private Account sourceAccount;
    private Account destinationAccount;


    public Transaction(
            Long transactionId,
            BigDecimal amount,
            LocalDateTime executedAt,
            TransactionType transactionType,
            TransactionStatus transactionStatus,
            Account sourceAccount,
            Account destinationAccount
    ) {
        validateAmount(amount);

        this.transactionId = transactionId;
        this.amount = amount;
        this.executedAt = executedAt;
        this.transactionType = Objects.requireNonNull(transactionType);
        this.transactionStatus = Objects.requireNonNull(transactionStatus);
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = Objects.requireNonNull(transactionType);
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = Objects.requireNonNull(transactionStatus);
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(Account sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public Account getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(Account destinationAccount) {
        this.destinationAccount = destinationAccount;
    }
}