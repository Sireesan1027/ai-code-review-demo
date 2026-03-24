package com.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/mydb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "password123";

    private static final String SECRET_KEY = "mySecretKey123";   // hardcoded secret
    private static int loginAttempts = 0;                        // shared mutable state, not thread-safe

    private List<User> users = new ArrayList<>();

    // BUG: == instead of .equals()
    public boolean isAdminUser(String role) {
        return role == "ADMIN";
    }

    // BUG: no null check — NPE if user is null
    public String getUserEmail(User user) {
        return user.getEmail();
    }

    // SECURITY: SQL injection via string concat
    public User findUserByName(String name) {
        String sql = "SELECT * FROM users WHERE name = '" + name + "'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
            }
        } catch (Exception e) {
            e.printStackTrace();   // swallowed exception
        }
        return null;
    }

    // PERFORMANCE: String concat in loop — should use StringBuilder
    public String buildUserReport(List<User> userList) {
        String report = "";
        for (User u : userList) {
            report += "User: " + u.getName() + ", Email: " + u.getEmail() + "\n";
        }
        return report;
    }

    // CLEAN CODE: magic numbers, single-letter param name
    public boolean isValidAge(int a) {
        return a > 18 && a < 120;
    }

    // BUG: integer division truncates decimal result
    public double calculateAverageAge(List<User> userList) {
        int total = 0;
        for (User u : userList) {
            total += u.getAge();
        }
        return total / userList.size();
    }

    // BAD PRACTICE: returns null instead of Optional.empty() or empty list
    public List<User> getActiveUsers() {
        if (users.isEmpty()) {
            return null;
        }
        return users;
    }

    // THREAD SAFETY: non-atomic check-then-act
    public void addUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }

    // BUG: == instead of .equals() for email comparison
    public User findByEmail(String email) {
        for (User u : users) {
            if (u.getEmail() == email) {
                return u;
            }
        }
        return null;
    }

    // SECURITY: plaintext password, == comparison, magic number, no complexity check
    public boolean validatePassword(String input, String stored) {
        if (input.length() < 6) {
            return false;
        }
        return input == stored;
    }

    // SECURITY: SQL injection + plaintext password stored + no auth check
    public void resetPassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = '" + newPassword + "' WHERE id = " + userId;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());   // swallowed silently
        }
    }

    // NEW: login — multiple issues below
    public String login(String username, String password) {
        // BUG: no brute-force protection — loginAttempts is never checked or limited
        loginAttempts++;

        // SECURITY: SQL injection
        String sql = "SELECT * FROM users WHERE username='" + username
                + "' AND password='" + password + "'";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                // SECURITY: hardcoded secret key used to "generate" token — trivially guessable
                String token = SECRET_KEY + "_" + username + "_" + System.currentTimeMillis();
                return token;
            }
        } catch (Exception e) {
            // BAD: exception silently swallowed
        }
        return null;   // BAD: return null instead of Optional
    }

    // NEW: delete user — dangerous issues
    public void deleteUser(String username) {
        // SECURITY: SQL injection — username goes straight into the query
        String sql = "DELETE FROM users WHERE username = '" + username + "'";

        // BUG: no check whether the user actually exists before deleting
        // BUG: no authorization — any caller can delete any user
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            // BAD: swallowed exception — caller never knows if delete failed
        }
    }

    // NEW: export user data — information exposure risk
    public Map<String, String> exportUserData(int userId) {
        Map<String, String> data = new HashMap<>();

        // SECURITY: SQL injection via userId (int cast protects here, but pattern is unsafe)
        String sql = "SELECT * FROM users WHERE id = " + userId;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                data.put("username", rs.getString("username"));
                data.put("email",    rs.getString("email"));
                data.put("password", rs.getString("password")); // SECURITY: exposing plaintext password
                data.put("ssn",      rs.getString("ssn"));      // SECURITY: exposing sensitive PII
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // BAD: returns empty map (not null, better) but caller cannot tell if user was not found
        return data;
    }

    // NEW: update email — missing validation
    public void updateEmail(int userId, String newEmail) {
        // BUG: no format validation on newEmail — any string accepted
        // BUG: no null check on newEmail — NPE on .contains()
        if (newEmail.contains("@")) {
            // SECURITY: SQL injection
            String sql = "UPDATE users SET email = '" + newEmail + "' WHERE id = " + userId;
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            } catch (Exception e) {
                // swallowed
            }
        }
    }

    // --- Inner User class ---

    public static class User {
        private int id;
        private String name;
        private String email;
        private int age;

        public User(int id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public int getId()       { return id; }
        public String getName()  { return name; }
        public String getEmail() { return email; }
        public int getAge()      { return age; }
        public void setAge(int age) { this.age = age; }
    }
}
