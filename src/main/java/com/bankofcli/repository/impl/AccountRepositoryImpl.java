package com.bankofcli.repository.impl;

import com.bankofcli.enums.AccountType;
import com.bankofcli.exception.BankException;
import com.bankofcli.model.Account;
import com.bankofcli.model.User;
import com.bankofcli.repository.contract.AccountRepository;
import com.bankofcli.repository.contract.UserRepository;
import com.bankofcli.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC FLOW
 *
 * 1. Connection
 *    Borrow connection from HikariCP
 *
 * 2. PreparedStatement
 *    Prepare SQL and set ? values
 *
 * 3. executeUpdate() / executeQuery()
 *    Send SQL to database
 *
 * 4. ResultSet
 *    Read returned rows
 *
 * 5. try-with-resources
 *    Automatically closes ResultSet,
 *    PreparedStatement and Connection
 *
 * 6. Connection.close()
 *    Returns connection to HikariCP pool
 */

public class AccountRepositoryImpl implements AccountRepository {

    private final UserRepository userRepository;

    public AccountRepositoryImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Account save(Account account) {

        String sql = """
                INSERT INTO my_bank.accounts
                (user_id, pin_hash, account_type, balance)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {

            ps.setLong(1, account.getUser().getUserId());
            ps.setString(2, account.getPinHash());
            ps.setString(3, account.getType().name());
            ps.setBigDecimal(4, account.getBalance());

            ps.executeUpdate();

            // Get generated account_id
            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {

                    // Column 1 contains generated key
                    // getLong() returns it as Java long
                    account.setAccountId(rs.getLong(1));
                }
            }

            return account;

        } catch (SQLException e) {
            throw new BankException(
                    "Could not save account", e
            );
        }
    }


    @Override
    public Optional<Account> findById(long accountId) {

        String sql =
                "SELECT * FROM my_bank.accounts WHERE account_id = ?";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setLong(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    User user = userRepository
                            .findById(rs.getLong("user_id"))
                            .orElseThrow(() ->
                                    new BankException("User not found")
                            );

                    Account account = new Account(
                            rs.getLong("account_id"),
                            rs.getString("pin_hash"),

                            // Convert DB String into AccountType enum
                            AccountType.valueOf(
                                    rs.getString("account_type")
                            ),

                            rs.getBigDecimal("balance"),

                            rs.getTimestamp("created_at")
                                    .toLocalDateTime(),

                            user
                    );

                    return Optional.of(account);
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new BankException(
                    "Could not find account",
                    e
            );
        }
    }


    @Override
    public List<Account> findByUserId(long userId) {

        String sql =
                "SELECT * FROM my_bank.accounts WHERE user_id = ?";

        List<Account> accounts = new ArrayList<>();

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setLong(1, userId);

            User user = userRepository
                    .findById(userId)
                    .orElseThrow(() ->
                            new BankException("User not found")
                    );

            try (ResultSet rs = ps.executeQuery()) {

                // Move through every returned row
                while (rs.next()) {

                    Account account = new Account(
                            rs.getLong("account_id"),
                            rs.getString("pin_hash"),

                            AccountType.valueOf(
                                    rs.getString("account_type")
                            ),

                            rs.getBigDecimal("balance"),

                            rs.getTimestamp("created_at")
                                    .toLocalDateTime(),

                            user
                    );

                    accounts.add(account);
                }
            }

            return accounts;

        } catch (SQLException e) {
            throw new BankException(
                    "Could not load accounts",
                    e
            );
        }
    }


    @Override
    public void updateBalance(
            long accountId,
            BigDecimal balance
    ) {

        String sql = """
                UPDATE my_bank.accounts
                SET balance = ?
                WHERE account_id = ?
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setBigDecimal(1, balance);
            ps.setLong(2, accountId);

            // executeUpdate returns number of affected rows
            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new BankException(
                        "Account not found: " + accountId
                );
            }

        } catch (SQLException e) {
            throw new BankException(
                    "Could not update balance", e
            );
        }
    }
}