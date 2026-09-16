package com.bankofcli.repository.impl;

import com.bankofcli.repository.TransactionRepository;

import com.bankofcli.enums.TransactionStatus;
import com.bankofcli.enums.TransactionType;
import com.bankofcli.exception.BankException;
import com.bankofcli.model.Transaction;
import com.bankofcli.util.ConnectionProvider;
import com.bankofcli.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class JdbcTransactionRepository implements TransactionRepository {
    private static final String INSERT_SQL = """
            INSERT INTO transactions
                (source_account_id, destination_account_id, amount, executed_at,
                 transaction_type, transaction_status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private final ConnectionProvider connectionProvider;

    public JdbcTransactionRepository() {
        this(DBConnection::getConnection);
    }

    public JdbcTransactionRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Transaction save(Transaction transaction) {
        try (Connection connection = connectionProvider.getConnection()) {
            return save(connection, transaction);
        } catch (SQLException exception) {
            throw new BankException("Unable to save transaction.", exception);
        }
    }

    @Override
    public Transaction save(Connection connection, Transaction transaction) {
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setNullableLong(statement, 1, transaction.getSourceAccountId());
            setNullableLong(statement, 2, transaction.getDestinationAccountId());
            statement.setBigDecimal(3, transaction.getAmount());
            statement.setTimestamp(4, java.sql.Timestamp.valueOf(transaction.getExecutedAt()));
            statement.setString(5, transaction.getType().name());
            statement.setString(6, transaction.getStatus().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Creating transaction did not return an ID.");
                }
                transaction.setTransactionId(keys.getLong(1));
            }
            return transaction;
        } catch (SQLException exception) {
            throw new BankException("Unable to save transaction.", exception);
        }
    }

    @Override
    public List<Transaction> findByAccountId(long accountId, int limit) {
        String sql = """
                SELECT transaction_id, source_account_id, destination_account_id,
                       amount, executed_at, transaction_type, transaction_status
                FROM transactions
                WHERE source_account_id = ? OR destination_account_id = ?
                ORDER BY executed_at DESC, transaction_id DESC
                LIMIT ?
                """;
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            statement.setLong(2, accountId);
            statement.setInt(3, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(map(resultSet));
                }
            }
            return transactions;
        } catch (SQLException exception) {
            throw new BankException("Unable to load transaction history.", exception);
        }
    }

    private void setNullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setLong(index, value);
        }
    }

    private Transaction map(ResultSet resultSet) throws SQLException {
        long source = resultSet.getLong("source_account_id");
        Long sourceId = resultSet.wasNull() ? null : source;
        long destination = resultSet.getLong("destination_account_id");
        Long destinationId = resultSet.wasNull() ? null : destination;
        return new Transaction(
                resultSet.getLong("transaction_id"),
                sourceId,
                destinationId,
                resultSet.getBigDecimal("amount"),
                resultSet.getTimestamp("executed_at").toLocalDateTime(),
                TransactionType.valueOf(resultSet.getString("transaction_type")),
                TransactionStatus.valueOf(resultSet.getString("transaction_status"))
        );
    }
}
