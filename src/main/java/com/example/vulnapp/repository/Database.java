package com.example.vulnapp.repository;

import com.example.vulnapp.config.DBConfig;
import com.example.vulnapp.model.Upload;
import com.example.vulnapp.model.User;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Database {

    private static final String JDBC_URL = "jdbc:h2:./vulnappdb";
    private static String JDBC_USER;
    private static String JDBC_PASS;

    @Autowired
    private DBConfig dbConfig;

    @PostConstruct
    public void init() {
        JDBC_USER = dbConfig.getUsername();
        JDBC_PASS = dbConfig.getPassword();

        try {
            Class.forName("org.h2.Driver");
            try (Connection conn = getConnection(); Statement st = conn.createStatement()) {

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

                st.execute(
                        "CREATE TABLE IF NOT EXISTS pings (" +
                                " id IDENTITY PRIMARY KEY, " +
                                " from_id BIGINT, " +
                                " to_id   BIGINT, " +
                                " sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                " FOREIGN KEY (from_id) REFERENCES users(id), " +
                                " FOREIGN KEY (to_id)   REFERENCES users(id))");
            }
        } catch (Exception ex) {
            LoggerFactory.getLogger(Database.class).error("Database failure", ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
    }

    public static void createUser(String username, String password) throws SQLException {
        if (username == null || !username.matches("^[a-zA-Z0-9_.-]{3,30}$")) {
            throw new IllegalArgumentException("Invalid username format");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Invalid password format");
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (username, password) VALUES (?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, md5(password));
            ps.executeUpdate();
        }
    }

    public static User validateUser(String username, String password) throws SQLException {
        if (username == null || !username.matches("^[a-zA-Z0-9_.-]{3,30}$")) {
            throw new IllegalArgumentException("Invalid username format");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Invalid password format");
        }

        String hash = md5(password);
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, hash);

            try (ResultSet rs = ps.executeQuery()) {
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

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            // converti in esadecimale
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void changeUsername(long userId, String newName) throws SQLException {
        if (newName == null || !newName.matches("^[a-zA-Z0-9_.-]{3,30}$")) {
            throw new IllegalArgumentException("Invalid username format");
        }

        String q = "UPDATE users SET username = ? WHERE id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setString(1, newName);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    public static void addPing(long fromId, long toId) throws SQLException {
        String q = "INSERT INTO pings (from_id, to_id) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setLong(1, fromId);
            ps.setLong(2, toId);
            ps.executeUpdate();
        }
    }

    public static List<User> getOtherUsers(long myId) throws SQLException {
        List<User> list = new ArrayList<>();
        String q = "SELECT * FROM users WHERE id <> " + myId;
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                list.add(u);
            }
        }
        return list;
    }

    public static List<Map<String,Object>> getSenders(long myId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();

        String q = "SELECT u.username, t.sent_at " +
                "FROM pings t JOIN users u ON t.from_id = u.id " +
                "WHERE t.to_id = " + myId +
                " ORDER BY t.sent_at DESC";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(q)) {
            while (rs.next()) {
                Map<String,Object> row = new HashMap<>();
                row.put("user", rs.getString(1));
                row.put("ts",   rs.getTimestamp(2));
                list.add(row);
            }
        }
        return list;
    }

}
