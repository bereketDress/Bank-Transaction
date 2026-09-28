package com.bankofcli.model;

import com.bankofcli.enums.LogLevel;

import java.time.LocalDateTime;
import java.util.Objects;

public class SystemLog {

    private Long logId;
    private LogLevel level;
    private String message;
    private LocalDateTime createdAt;
    private User user;

    public SystemLog(Long logId, LogLevel level, String message, LocalDateTime createdAt, User user) {
        this.logId = logId;
        this.level = Objects.requireNonNull(level);
        this.message = Objects.requireNonNull(message);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.user = user;

    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = Objects.requireNonNull(level);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = Objects.requireNonNull(message);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}