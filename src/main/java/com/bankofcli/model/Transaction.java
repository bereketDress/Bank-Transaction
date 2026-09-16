package com.bankofcli.model;

import com.bankofcli.enums.TransactionStatus;
import com.bankofcli.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private Long transactionId;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private LocalDateTime executedAt;
    private TransactionType type;
    private TransactionStatus status;

    public Transaction() {
    }

    public Transaction(Long transactionId, Long sourceAccountId, Long destinationAccountId,
                       BigDecimal amount, LocalDateTime executedAt,
                       TransactionType type, TransactionStatus status) {
        this.transactionId = transactionId;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.executedAt = executedAt;
        this.type = type;
        this.status = status;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
