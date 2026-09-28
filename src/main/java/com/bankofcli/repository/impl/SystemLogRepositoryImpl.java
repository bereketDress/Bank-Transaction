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

public class JdbcSystemLogRepository implements SystemLogRepository {

    private final UserRepository userRepository;

    public JdbcSystemLogRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public SystemLog save(SystemLog log) {

        String sql = """
                INSERT INTO system_logs (user_id, log_level, message)
                VALUES (?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, log.getUser().getUserId());
            ps.setString(2, log.getLevel().name());
            ps.setString(3, log.getMessage());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                log.setLogId(rs.getLong(1));
            }

            return log;

        } catch (SQLException e) {
            throw new BankException("Could not save log", e);
        }
    }

    @Override
    public List<SystemLog> findByUserId(long userId, int limit) {

        String sql = """
                SELECT *
                FROM system_logs
                WHERE user_id = ?
                ORDER BY created_at DESC
                LIMIT ?
                """;

        List<SystemLog> logs = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setInt(2, limit);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                User user = userRepository.findById(rs.getLong("user_id"))
                        .orElseThrow(() -> new BankException("User not found"));

                logs.add(new SystemLog(
                        rs.getLong("log_id"),
                        LogLevel.valueOf(rs.getString("log_level")),
                        rs.getString("message"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        user
                ));
            }

            return logs;

        } catch (SQLException e) {
            throw new BankException("Could not load logs", e);
        }
    }
}