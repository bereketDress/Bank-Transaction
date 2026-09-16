package com.bankofcli.util;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}
