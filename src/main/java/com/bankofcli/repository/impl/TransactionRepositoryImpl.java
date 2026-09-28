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

public class TransactionRepositoryImpl implements TransactionRepository {

    private final AccountRepository accountRepository;

    public TransactionRepositoryImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {

        String sql = """
                INSERT INTO my_bank.transactions
                (source_account_id, destination_account_id, amount,
                 executed_at, transaction_type, transaction_status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection con = DBConnection.getConnection();

                PreparedStatement ps = con.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            // Set source account id or SQL NULL
            setAccountId(
                    ps,
                    1,
                    transaction.getSourceAccount()
            );

            // Set destination account id or SQL NULL
            setAccountId(
                    ps,
                    2,
                    transaction.getDestinationAccount()
            );

            ps.setBigDecimal(
                    3,
                    transaction.getAmount()
            );

            ps.setTimestamp(
                    4,
                    Timestamp.valueOf(transaction.getExecutedAt())
            );

            // Convert enum to String
            ps.setString(
                    5,
                    transaction.getTransactionType().name()
            );

            // Convert enum to String
            ps.setString(
                    6,
                    transaction.getTransactionStatus().name()
            );

            ps.executeUpdate();

            // Get generated transaction_id
            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    transaction.setTransactionId(
                            rs.getLong(1)
                    );
                }
            }

            return transaction;

        } catch (SQLException e) {
            throw new BankException(
                    "Could not save transaction",
                    e
            );
        }
    }


    @Override
    public List<Transaction> findByAccountId(
            long accountId,
            int limit
    ) {

        String sql = """
                SELECT *
                FROM my_bank.transactions
                WHERE source_account_id = ?
                   OR destination_account_id = ?
                ORDER BY executed_at DESC
                LIMIT ?
                """;

        List<Transaction> transactions =
                new ArrayList<>();

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            // Same account can be source
            ps.setLong(1, accountId);

            // Or destination
            ps.setLong(2, accountId);

            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    // Convert each DB row into Transaction object
                    transactions.add(map(rs));
                }
            }

            return transactions;

        } catch (SQLException e) {
            throw new BankException(
                    "Could not load transactions",
                    e
            );
        }
    }


    // Convert one ResultSet row into a Transaction object
    private Transaction map(ResultSet rs)
            throws SQLException {

        // Read nullable source account id
        Long sourceId =
                getId(rs, "source_account_id");

        // Read nullable destination account id
        Long destinationId =
                getId(rs, "destination_account_id");

        // Load source account if sourceId exists
        Account source =
                sourceId == null
                        ? null
                        : accountRepository
                        .findById(sourceId)
                        .orElse(null);

        // Load destination account if destinationId exists
        Account destination =
                destinationId == null
                        ? null
                        : accountRepository
                        .findById(destinationId)
                        .orElse(null);

        return new Transaction(
                rs.getLong("transaction_id"),

                rs.getBigDecimal("amount"),

                rs.getTimestamp("executed_at")
                        .toLocalDateTime(),

                // String -> enum
                TransactionType.valueOf(
                        rs.getString("transaction_type")
                ),

                // String -> enum
                TransactionStatus.valueOf(
                        rs.getString("transaction_status")
                ),

                source,
                destination
        );
    }


    // Put Account ID into PreparedStatement.
    // If account is null, put SQL NULL.
    private void setAccountId(
            PreparedStatement ps,
            int index,
            Account account
    ) throws SQLException {

        if (account == null) {

            ps.setNull(
                    index,
                    Types.BIGINT
            );

        } else {

            ps.setLong(
                    index,
                    account.getAccountId()
            );
        }
    }


    // Read a nullable BIGINT account id from ResultSet
    private Long getId(
            ResultSet rs,
            String column
    ) throws SQLException {

        long id = rs.getLong(column);

        // getLong() returns 0 when SQL value is NULL,
        // so wasNull() tells us whether it was actually NULL.
        return rs.wasNull()
                ? null
                : id;
    }
}