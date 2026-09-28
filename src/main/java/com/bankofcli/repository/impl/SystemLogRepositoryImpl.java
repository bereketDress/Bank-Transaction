package com.bankofcli.repository.impl;

import com.bankofcli.enums.LogLevel;
import com.bankofcli.exception.BankException;
import com.bankofcli.model.SystemLog;
import com.bankofcli.model.User;
import com.bankofcli.repository.contract.SystemLogRepository;
import com.bankofcli.repository.contract.UserRepository;
import com.bankofcli.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SystemLogRepositoryImpl implements SystemLogRepository {

    private final UserRepository userRepository;

    public SystemLogRepositoryImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public SystemLog save(SystemLog log) {

        String sql = """
                INSERT INTO my_bank.system_logs
                (user_id, log_level, message)
                VALUES (?, ?, ?)
                """;

        try (
                Connection con = DBConnection.getConnection();

                PreparedStatement ps = con.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            ps.setLong(1, log.getUser().getUserId());
            ps.setString(2, log.getLevel().name());
            ps.setString(3, log.getMessage());

            ps.executeUpdate();

            // Read generated log_id
            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    log.setLogId(rs.getLong(1));
                }
            }

            return log;

        } catch (SQLException e) {
            throw new BankException(
                    "Could not save log",
                    e
            );
        }
    }

    @Override
    public List<SystemLog> findByUserId(long userId, int limit) {

        String sql = """
                SELECT *
                FROM my_bank.system_logs
                WHERE user_id = ?
                ORDER BY created_at DESC
                LIMIT ?
                """;

        List<SystemLog> logs = new ArrayList<>();

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setLong(1, userId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {

                User user = null;

                while (rs.next()) {

                    // Load the user only once
                    if (user == null) {
                        user = userRepository
                                .findById(userId)
                                .orElseThrow(() ->
                                        new BankException("User not found")
                                );
                    }

                    SystemLog log = new SystemLog(
                            rs.getLong("log_id"),

                            // Convert database String to LogLevel enum
                            LogLevel.valueOf(
                                    rs.getString("log_level")
                            ),

                            rs.getString("message"),

                            rs.getTimestamp("created_at")
                                    .toLocalDateTime(),

                            user
                    );

                    logs.add(log);
                }
            }

            return logs;

        } catch (SQLException e) {
            throw new BankException(
                    "Could not load logs",
                    e
            );
        }
    }
}