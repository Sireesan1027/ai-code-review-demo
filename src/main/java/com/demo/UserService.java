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
     * This will fail for non-interned strings at runtime.
     */
    public boolean isAdminUser(String role) {
        return role == "ADMIN"; // BUG: should be role.equals("ADMIN")
    }

    /**
     * BUG: no null check on the user parameter.
     * Calling getUserEmail(null) will throw NullPointerException.
     */
    public String getUserEmail(User user) {
        return user.getEmail(); // BUG: no null check before dereferencing
    }

    /**
     * SECURITY VULNERABILITY: raw SQL string concatenation → SQL injection.
     * An attacker can pass: "'; DROP TABLE users; --"
     */
    public User findUserByName(String name) {
        // VULNERABILITY: never concatenate user input directly into SQL
        String sql = "SELECT * FROM users WHERE name = '" + name + "'";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
            }
        } catch (Exception e) {
            // BAD PRACTICE: swallowing exception silently
            e.printStackTrace();
        }
        return null;
    }

    /**
     * PERFORMANCE ISSUE: inefficient string concatenation in a loop.
     * Should use StringBuilder instead.
     */
    public String buildUserReport(List<User> userList) {
        String report = ""; // BAD: String is immutable; each += creates a new object
        for (User u : userList) {
            report += "User: " + u.getName() + ", Email: " + u.getEmail() + "\n";
        }
        return report;
    }

    /**
     * CLEAN CODE VIOLATION: magic numbers, unclear variable names.
     */
    public boolean isValidAge(int a) {
        return a > 18 && a < 120; // magic numbers 18 and 120 should be named constants
    }

    /**
     * BUG: integer division truncates result; should use double or BigDecimal.
     */
    public double calculateAverageAge(List<User> userList) {
        int total = 0;
        for (User u : userList) {
            total += u.getAge();
        }
        return total / userList.size(); // BUG: integer division loses precision
    }

    /**
     * BAD PRACTICE: returns null instead of Optional or empty list.
     * Callers must always null-check, which is easy to forget.
     */
    public List<User> getActiveUsers() {
        if (users.isEmpty()) {
            return null; // BAD: return Collections.emptyList() instead
        }
        return users;
    }

    /**
     * THREAD SAFETY ISSUE: non-atomic check-then-act on a shared list.
     * In a multi-threaded context this can cause duplicates.
     */
    public void addUser(User user) {
        if (!users.contains(user)) { // RACE CONDITION: another thread may add between check and add
            users.add(user);
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
