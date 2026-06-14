package com.ordering.system.dao;

import java.sql.*;

/**
 * SQLConn is a Singleton class that manages the connection to the MySQL database.
 * It provides helper methods to reduce redundant code in DAO classes.
 */
public class SQLConn {
    private static SQLConn instance = null;
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Database credentials - Adjust these to your local MySQL settings
    private final String host = "localhost";
    private final int port = 3306;
    private final String database = "dborsys"; 
    private final String username = "root"; 
    private final String password = "put password here or else."; 

    private Connection connection;

    private SQLConn() {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    public static synchronized SQLConn getInstance() {
        if (instance == null) {
            instance = new SQLConn();
        }
        return instance;
    }

    public void connect() throws SQLException {
        if (isConnected()) return;
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", 
                host, port, database);
        connection = DriverManager.getConnection(url, username, password);
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void close() throws SQLException {
        if (isConnected()) {
            connection.close();
            connection = null;
        }
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        connect();
        PreparedStatement stmt = connection.prepareStatement(sql);
        setParameters(stmt, params);
        return stmt.executeQuery();
    }

    public int executeUpdateDelete(String sql, Object... params) throws SQLException {
        connect();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        }
    }

    public long executeInsert(String sql, Object... params) throws SQLException {
        connect();
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(stmt, params);
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) return generatedKeys.getLong(1);
            }
        }
        return -1;
    }

    public Connection getConnection() throws SQLException {
        connect();
        return connection;
    }
}