package com.bankofcli.repository.impl;

import com.bankofcli.repository.AccountRepository;

import com.bankofcli.enums.AccountType;
import com.bankofcli.exception.BankException;
import com.bankofcli.model.Account;
import com.bankofcli.util.ConnectionProvider;
import com.bankofcli.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class JdbcAccountRepository implements AccountRepository {
    private static final String COLUMNS =
            "account_id, user_id, pin_hash, account_type, balance, created_at";
    private final ConnectionProvider connectionProvider;

    public JdbcAccountRepository() {
        this(DBConnection::getConnection);
    }

    public JdbcAccountRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Account save(Account account) {
        String sql = "INSERT INTO accounts (user_id, pin_hash, account_type, balance) VALUES (?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, account.getUserId());
            statement.setString(2, account.getPinHash());
            statement.setString(3, account.getType().name());
            statement.setBigDecimal(4, account.getBalance());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Creating account did not return an ID.");
                }
                account.setAccountId(keys.getLong(1));
            }
            return account;
        } catch (SQLException exception) {
            throw new BankException("Unable to create account.", exception);
        }
    }

    @Override
    public Optional<Account> findById(long accountId) {
        String sql = "SELECT " + COLUMNS + " FROM accounts WHERE account_id = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            return queryOne(statement);
        } catch (SQLException exception) {
            throw new BankException("Unable to load account.", exception);
        }
    }

    @Override
    public Optional<Account> findByIdForUpdate(Connection connection, long accountId) {
        String sql = "SELECT " + COLUMNS + " FROM accounts WHERE account_id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            return queryOne(statement);
        } catch (SQLException exception) {
            throw new BankException("Unable to lock account.", exception);
        }
    }

    @Override
    public List<Account> findByUserId(long userId) {
        String sql = "SELECT " + COLUMNS + " FROM accounts WHERE user_id = ? ORDER BY account_id";
        List<Account> accounts = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    accounts.add(map(resultSet));
                }
            }
            return accounts;
        } catch (SQLException exception) {
            throw new BankException("Unable to load accounts.", exception);
        }
    }

    @Override
    public void updateBalance(Connection connection, long accountId, BigDecimal newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, newBalance);
            statement.setLong(2, accountId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Expected to update one account.");
            }
        } catch (SQLException exception) {
            throw new BankException("Unable to update account balance.", exception);
        }
    }

    private Optional<Account> queryOne(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
        }
    }

    private Account map(ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getLong("account_id"),
                resultSet.getString("pin_hash"),
                AccountType.valueOf(resultSet.getString("account_type")),
                resultSet.getBigDecimal("balance"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getLong("user_id")
        );
    }
}
