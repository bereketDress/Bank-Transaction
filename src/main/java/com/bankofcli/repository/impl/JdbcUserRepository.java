package com.bankofcli.repository.impl;

import com.bankofcli.repository.UserRepository;

import com.bankofcli.exception.BankException;
import com.bankofcli.model.User;
import com.bankofcli.util.ConnectionProvider;
import com.bankofcli.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {
    private final ConnectionProvider connectionProvider;

    public JdbcUserRepository() {
        this(DBConnection::getConnection);
    }

    public JdbcUserRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (user_name, user_email, password_hash) VALUES (?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getUserEmail());
            statement.setString(3, user.getPasswordHash());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Creating user did not return an ID.");
                }
                user.setUserId(keys.getLong(1));
            }
            return user;
        } catch (SQLException exception) {
            throw new BankException("Unable to create user.", exception);
        }
    }

    @Override
    public Optional<User> findById(long userId) {
        return findOne("SELECT user_id, user_name, user_email, password_hash FROM users WHERE user_id = ?",
                statement -> statement.setLong(1, userId));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findOne("SELECT user_id, user_name, user_email, password_hash FROM users WHERE LOWER(user_email) = LOWER(?)",
                statement -> statement.setString(1, email));
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE LOWER(user_email) = LOWER(?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new BankException("Unable to check user email.", exception);
        }
    }

    private Optional<User> findOne(String sql, StatementBinder binder) {
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new BankException("Unable to load user.", exception);
        }
    }

    private User map(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("user_id"),
                resultSet.getString("user_name"),
                resultSet.getString("user_email"),
                resultSet.getString("password_hash")
        );
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
