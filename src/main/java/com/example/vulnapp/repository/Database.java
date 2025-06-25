package com.example.vulnapp.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.vulnapp.model.Upload;
import com.example.vulnapp.model.User;

public class Database {

    private static final String JDBC_URL = "jdbc:h2:./vulnappdb";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASS = "";

    static {
        try {
            Class.forName("org.h2.Driver");
            try (Connection conn = getConnection();
                 Statement st = conn.createStatement()) {

                st.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id IDENTITY PRIMARY KEY, " +
                        "username VARCHAR(100) UNIQUE NOT NULL, " +
                        "password VARCHAR(100) NOT NULL)");

                st.execute("CREATE TABLE IF NOT EXISTS uploads (" +
                        "id IDENTITY PRIMARY KEY, " +
                        "filename VARCHAR(255), " +
                        "content CLOB, " +
                        "user_id BIGINT, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id))");

                st.execute("CREATE TABLE IF NOT EXISTS messages (" +
                                " id IDENTITY PRIMARY KEY, " +
                                " content CLOB NOT NULL, " +
                                " user_id BIGINT, " +
                                " created TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                " FOREIGN KEY (user_id) REFERENCES users(id))");

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
    }

    public static void createUser(String username, String password) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (username, password) VALUES (?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, password); // Plain‑text password (insecure)
            ps.executeUpdate();
        }
    }

    // Vulnerable: SQL concatenation allows SQL Injection
    public static User validateUser(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = '" + username +
                       "' AND password = '" + password + "'";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                return u;
            }
            return null;
        }
    }

    public static void addUpload(String filename, String content, long userId) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO uploads (filename, content, user_id) VALUES (?, ?, ?)")) {
            ps.setString(1, filename);
            ps.setString(2, content);
            ps.setLong(3, userId);
            ps.executeUpdate();
        }
    }

    public static List<Upload> getAllUploads() throws SQLException {
        List<Upload> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM uploads")) {
            while (rs.next()) {
                Upload up = new Upload();
                up.setId(rs.getLong("id"));
                up.setFilename(rs.getString("filename"));
                up.setContent(rs.getString("content"));
                up.setUserId(rs.getLong("user_id"));
                list.add(up);
            }
        }
        return list;
    }

    /* ========= quick-message helpers ========= */
    public static void addMessage(String content, long userId) throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO messages (content, user_id) VALUES (?, ?)")) {
            ps.setString(1, content);
            ps.setLong (2, userId);
            ps.executeUpdate();
        }
    }

    public static List<Map<String,String>> getAllMessages() throws SQLException {
        List<Map<String,String>> list = new ArrayList<>();
        String q = "SELECT m.id, m.content, u.username " +
                "FROM messages m JOIN users u ON m.user_id=u.id " +
                "ORDER BY m.created DESC";
        try (Connection c = getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) {
                Map<String,String> row = new HashMap<>();
                row.put("user", rs.getString("username"));
                row.put("content", rs.getString("content"));
                list.add(row);
            }
        }
        return list;
    }
}
