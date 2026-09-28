package com.bankofcli.repository.impl;

import com.bankofcli.exception.BankException;
import com.bankofcli.model.User;
import com.bankofcli.repository.contract.UserRepository;
import com.bankofcli.util.DBConnection;

import java.sql.*;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    @Override
    public User save(User user) {

        String sql = """
                INSERT INTO my_bank.users
                (user_name, user_email, password_hash)
                VALUES (?, ?, ?)
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getUserEmail());
            ps.setString(3, user.getPasswordHash());

            ps.executeUpdate();

            // Get generated user_id
            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    user.setUserId(rs.getLong(1));
                }
            }

            return user;

        } catch (SQLException e) {
            throw new BankException(
                    "Could not save user",
                    e
            );
        }
    }

    @Override
    public Optional<User> findById(long userId) {

        String sql = """
                SELECT *
                FROM my_bank.users
                WHERE user_id = ?
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(map(rs));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new BankException(
                    "Could not find user",
                    e
            );
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {

        String sql = """
                SELECT *
                FROM my_bank.users
                WHERE user_email = ?
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(map(rs));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new BankException(
                    "Could not find user",
                    e
            );
        }
    }

    // Convert one database row into a User object
    private User map(ResultSet rs) throws SQLException {

        return new User(
                rs.getLong("user_id"),
                rs.getString("user_name"),
                rs.getString("user_email"),
                rs.getString("password_hash")
        );
    }
}