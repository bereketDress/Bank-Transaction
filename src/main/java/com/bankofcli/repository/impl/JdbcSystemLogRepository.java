package com.bankofcli.repository.impl;

import com.bankofcli.repository.SystemLogRepository;

import com.bankofcli.enums.LogLevel;
import com.bankofcli.exception.BankException;
import com.bankofcli.model.SystemLog;
import com.bankofcli.util.ConnectionProvider;
import com.bankofcli.util.DBConnection;

import java.sql.*;
import java.util.*;

public class JdbcSystemLogRepository implements SystemLogRepository {
    private final ConnectionProvider connectionProvider;

    public JdbcSystemLogRepository() {
        this(DBConnection::getConnection);
    }

    public JdbcSystemLogRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public SystemLog save(SystemLog systemLog) {
        String sql = "INSERT INTO system_logs (user_id, log_level, message, created_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (systemLog.getUserId() == null) {
                statement.setNull(1, Types.BIGINT);
            } else {
                statement.setLong(1, systemLog.getUserId());
            }
            statement.setString(2, systemLog.getLevel().name());
            statement.setString(3, systemLog.getMessage());
            statement.setTimestamp(4, java.sql.Timestamp.valueOf(systemLog.getCreatedAt()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Creating system log did not return an ID.");
                }
                systemLog.setLogId(keys.getLong(1));
            }
            return systemLog;
        } catch (SQLException exception) {
            throw new BankException("Unable to save system log.", exception);
        }
    }

    @Override
    public List<SystemLog> findByUserId(long userId, int limit) {
        String sql = """
                SELECT log_id, user_id, log_level, message, created_at
                FROM system_logs
                WHERE user_id = ?
                ORDER BY created_at DESC, log_id DESC
                LIMIT ?
                """;
        List<SystemLog> logs = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setInt(2, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(new SystemLog(
                            resultSet.getLong("log_id"),
                            LogLevel.valueOf(resultSet.getString("log_level")),
                            resultSet.getString("message"),
                            resultSet.getTimestamp("created_at").toLocalDateTime(),
                            resultSet.getLong("user_id")
                    ));
                }
            }
            return logs;
        } catch (SQLException exception) {
            throw new BankException("Unable to load system logs.", exception);
        }
    }
}
