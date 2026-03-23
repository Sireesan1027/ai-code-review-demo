package com.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * UserService - manages user operations.
 *
 * NOTE: This class intentionally contains bugs and bad practices
 * so that Claude AI has real issues to catch during code review.
 */
public class UserService {

    // BAD PRACTICE: hardcoded credentials in source code
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mydb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "password123";

    private List<User> users = new ArrayList<>();

    /**
     * BUG: uses == instead of .equals() for String comparison.
     */
    public boolean isAdminUser(String role) {
        return role == "ADMIN";
    }

    /**
     * BUG: no null check on the user parameter.
     */
    public String getUserEmail(User user) {
        return user.getEmail();
    }

    /**
     * SECURITY VULNERABILITY: SQL injection via string concatenation.
     */
    public User findUserByName(String name) {
        String sql = "SELECT * FROM users WHERE name = '" + name + "'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * PERFORMANCE ISSUE: String concatenation in a loop.
     */
    public String buildUserReport(List<User> userList) {
        String report = "";
        for (User u : userList) {
            report += "User: " + u.getName() + ", Email: " + u.getEmail() + "\n";
        }
        return report;
    }

    /**
     * CLEAN CODE VIOLATION: magic numbers, poor variable name.
     */
    public boolean isValidAge(int a) {
        return a > 18 && a < 120;
    }

    /**
     * BUG: integer division loses decimal precision.
     */
    public double calculateAverageAge(List<User> userList) {
        int total = 0;
        for (User u : userList) {
            total += u.getAge();
        }
        return total / userList.size();
    }

    /**
     * BAD PRACTICE: returns null instead of Optional or empty list.
     */
    public List<User> getActiveUsers() {
        if (users.isEmpty()) {
            return null;
        }
        return users;
    }

    /**
     * THREAD SAFETY ISSUE: non-atomic check-then-act.
     */
    public void addUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }

    // ── NEW METHOD added in this PR ──────────────────────────────────────────

    /**
     * Validates a user password.
     * BUG: stores and compares passwords as plain text (no hashing).
     * BUG: weak minimum length check only — no complexity rules.
     * BUG: == used for String comparison again.
     */
    public boolean validatePassword(String input, String stored) {
        if (input.length() < 6) {           // magic number, too short
            return false;
        }
        return input == stored;             // BUG: == instead of .equals()
    }

    /**
     * Resets a user's password directly in the database.
     * SECURITY: no authorization check — any caller can reset any user's password.
     * SECURITY: new password written to DB in plain text (no hashing).
     * SECURITY: SQL injection via string concat.
     */
    public void resetPassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = '" + newPassword + "' WHERE id = " + userId;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            // BAD: silently swallowing the exception
            System.out.println("Error: " + e.getMessage());
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

        public int getId()      { return id; }
        public String getName() { return name; }
        public String getEmail(){ return email; }
        public int getAge()     { return age; }
        public void setAge(int age) { this.age = age; }
    }
}
