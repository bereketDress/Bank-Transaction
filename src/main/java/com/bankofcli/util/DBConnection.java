package com.bankofcli.util;

import com.bankofcli.exception.BankException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection class creates and manages the HikariCP connection pool
 * and DAO use created pool
 */


public final class DBConnection {

    private static HikariDataSource dataSource;

    private DBConnection() {}

    // Borrow a connection from the HikariCP pool
    public static synchronized Connection getConnection() throws SQLException {
        if (dataSource == null) {
            dataSource = createDataSource();
        }

        return dataSource.getConnection();
    }

    // Shut down the entire HikariCP connection pool.
    public static synchronized void closePool() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    // Load database configuration and create the HikariCP connection pool.
    private static HikariDataSource createDataSource() {

        Properties pro = new Properties();

        // Load db.properties from the classpath.
        try (InputStream input = DBConnection.class // InputStream reads byte from file not collection
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new BankException("db.properties was not found on the classpath.");
            }

            pro.load(input);

        } catch (IOException e) {
            throw new BankException("Unable to load database configuration.", e);
        }

        // Configure HikariCP.
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(environment("DB_URL", pro.getProperty("db.url")));
        config.setUsername(environment("DB_USER", pro.getProperty("db.username")));
        config.setPassword(environment("DB_PASSWORD", pro.getProperty("db.password")));
        config.setMaximumPoolSize(Integer.parseInt(pro.getProperty("db.pool.maximumSize", "10")));
        config.setMinimumIdle(1);//Keep at least 1 idle connection ready for the next request
        config.setPoolName("bankofcli-pool");
        config.setAutoCommit(true);//Automatically commit each successful SQL statement

        // return Created HikariCP pool.
        return new HikariDataSource(config);
    }

    // use environment variable, else default value from db.properties.
    private static String environment(String name, String defaultValue) {
        String value = System.getenv(name);// getenv: builtin method from System class

        return value == null || value.isBlank() ? defaultValue : value;
    }
}










    /*
    HIKARICP USAGE IN DAO

    String sql = "SELECT name FROM employees WHERE id = ?";

    try (
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement(sql)
    ) {
        pst.setInt(1, 5);

        try (ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                System.out.println(rs.getString("name"));
            }

        }

    } // Java automatically calls rs.close(), pst.close() and con.close()
      // then HikariCP, con.close() returns the connection to the pool.
    */


    /*
    MANUAL JDBC WITHOUT HIKARICP

    String sql = "SELECT name FROM employees WHERE id = ?";
    String url = "jdbc:postgresql://localhost:5432/bank_of_cli";
    String user = "postgres";
    String password = "change_me";

    Connection con =DriverManager.getConnection(url, user, password);

    PreparedStatement pst =con.prepareStatement(sql);

    pst.setInt(1, 5);

    ResultSet rs = pst.executeQuery();

    if (rs.next()) {
    String n = rs.getString("name");
    System.out.println(n);
    }

    rs.close();
    pst.close();
    con.close();
    */