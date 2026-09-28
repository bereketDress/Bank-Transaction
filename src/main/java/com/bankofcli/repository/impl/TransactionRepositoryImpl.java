package com.bankofcli.repository.impl;

import com.bankofcli.enums.TransactionStatus;
import com.bankofcli.enums.TransactionType;
import com.bankofcli.exception.BankException;
import com.bankofcli.model.Account;
import com.bankofcli.model.Transaction;
import com.bankofcli.repository.contract.AccountRepository;
import com.bankofcli.repository.contract.TransactionRepository;
import com.bankofcli.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTransactionRepository implements TransactionRepository {

    private final AccountRepository accountRepository;

    public JdbcTransactionRepository(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {

        String sql = """
                INSERT INTO transactions
                (source_account_id, destination_account_id, amount,
                 executed_at, transaction_type, transaction_status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setAccountId(ps, 1, transaction.getSourceAccount());
            setAccountId(ps, 2, transaction.getDestinationAccount());

            ps.setBigDecimal(3, transaction.getAmount());
            ps.setTimestamp(4, Timestamp.valueOf(transaction.getExecutedAt()));
            ps.setString(5, transaction.getTransactionType().name());
            ps.setString(6, transaction.getTransactionStatus().name());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                transaction.setTransactionId(rs.getLong(1));
            }

            return transaction;

        } catch (SQLException e) {
            throw new BankException("Could not save transaction", e);
        }
    }

    @Override
    public List<Transaction> findByAccountId(long accountId, int limit) {

        String sql = """
                SELECT *
                FROM transactions
                WHERE source_account_id = ?
                   OR destination_account_id = ?
                ORDER BY executed_at DESC
                LIMIT ?
                """;

        List<Transaction> transactions = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, accountId);
            ps.setLong(2, accountId);
            ps.setInt(3, limit);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                transactions.add(map(rs));
            }

            return transactions;

        } catch (SQLException e) {
            throw new BankException("Could not load transactions", e);
        }
    }

    private Transaction map(ResultSet rs) throws SQLException {

        Long sourceId = getId(rs, "source_account_id");
        Long destinationId = getId(rs, "destination_account_id");

        Account source = sourceId == null ? null :
                accountRepository.findById(sourceId).orElse(null);

        Account destination = destinationId == null ? null :
                accountRepository.findById(destinationId).orElse(null);

        return new Transaction(
                rs.getLong("transaction_id"),
                rs.getBigDecimal("amount"),
                rs.getTimestamp("executed_at").toLocalDateTime(),
                TransactionType.valueOf(rs.getString("transaction_type")),
                TransactionStatus.valueOf(rs.getString("transaction_status")),
                source,
                destination
        );
    }

    private void setAccountId(
            PreparedStatement ps, int index, Account account) throws SQLException {

        if (account == null) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setLong(index, account.getAccountId());
        }
    }

    private Long getId(ResultSet rs, String column) throws SQLException {
        long id = rs.getLong(column);
        return rs.wasNull() ? null : id;
    }
}