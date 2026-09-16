package com.bankofcli.model;

import com.bankofcli.enums.LogLevel;

import java.time.LocalDateTime;

public class SystemLog {
    private Long logId;
    private LogLevel level;
    private String message;
    private LocalDateTime createdAt;
    private Long userId;

    public SystemLog() {
    }

    public SystemLog(Long logId, LogLevel level, String message,
                     LocalDateTime createdAt, Long userId) {
        this.logId = logId;
        this.level = level;
        this.message = message;
        this.createdAt = createdAt;
        this.userId = userId;
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
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
