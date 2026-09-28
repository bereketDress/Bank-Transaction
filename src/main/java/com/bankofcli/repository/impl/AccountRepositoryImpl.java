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

public class JdbcAccountRepository implements AccountRepository {

        private final UserRepository userRepository;

        public JdbcAccountRepository(UserRepository userRepository) {
            this.userRepository = userRepository;
        }
    @Override
    public Account save(Account account) {
        String sql = """
                INSERT INTO accounts
                (user_id, pin_hash, account_type, balance)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, account.getUser().getUserId());
            ps.setString(2, account.getPinHash());
            ps.setString(3, account.getType().name());
            ps.setBigDecimal(4, account.getBalance());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                account.setAccountId(rs.getLong(1));
            }

            return account;

        } catch (SQLException e) {
            throw new BankException("Could not save account", e);
        }
    }

    @Override
    public Optional<Account> findById(long accountId) {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, accountId);
            ResultSet rs = ps.executeQuery();

            return rs.next()
                    ? Optional.of(map(rs))
                    : Optional.empty();

        } catch (SQLException e) {
            throw new BankException("Could not find account", e);
        }
    }

    @Override
    public List<Account> findByUserId(long userId) {
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        List<Account> accounts = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                accounts.add(map(rs));
            }

            return accounts;

        } catch (SQLException e) {
            throw new BankException("Could not load accounts", e);
        }
    }

    @Override
    public void updateBalance(long accountId, BigDecimal balance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, balance);
            ps.setLong(2, accountId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new BankException("Could not update balance", e);
        }
    }

    private Account map(ResultSet rs) throws SQLException {

            long userId = rs.getLong("user_id");

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BankException("User not found"));

            return new Account(
                    rs.getLong("account_id"),
                    rs.getString("pin_hash"),
                    AccountType.valueOf(rs.getString("account_type")),
                    rs.getBigDecimal("balance"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    user
            );
        }
}