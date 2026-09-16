package com.bankofcli.util;

import com.bankofcli.exception.BankException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {
    private static HikariDataSource dataSource;

    private DBConnection() {
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (dataSource == null) {
            dataSource = createDataSource();
        }
        return dataSource.getConnection();
    }

    public static synchronized void closePool() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    private static HikariDataSource createDataSource() {
        Properties properties = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new BankException("db.properties was not found on the classpath.");
            }
            properties.load(input);
        } catch (IOException exception) {
            throw new BankException("Unable to load database configuration.", exception);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(environmentOrDefault("DB_URL", properties.getProperty("db.url")));
        config.setUsername(environmentOrDefault("DB_USER", properties.getProperty("db.username")));
        config.setPassword(environmentOrDefault("DB_PASSWORD", properties.getProperty("db.password")));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.pool.maximumSize", "10")));
        config.setMinimumIdle(1);
        config.setPoolName("bankofcli-pool");
        config.setAutoCommit(true);
        return new HikariDataSource(config);
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
