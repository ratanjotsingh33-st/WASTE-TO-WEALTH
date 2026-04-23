package com.w2w.service;

import com.w2w.db.DatabaseConnection;
import com.w2w.model.User;
import com.w2w.util.Validator;

import java.sql.*;

public class AuthService {
    private static AuthService instance;
    private User currentUser;
    private final Connection conn;

    private AuthService() {
        conn = DatabaseConnection.getInstance().getConnection();
    }

    public static AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    public User register(String name, String email, String password) {
        if (!Validator.isValidName(name)) {
            System.out.println("❌ Invalid name. Must be at least 2 characters.");
            return null;
        }
        if (!Validator.isValidEmail(email)) {
            System.out.println("❌ Invalid email format.");
            return null;
        }
        if (!Validator.isValidPassword(password)) {
            System.out.println("❌ Password must be at least 6 characters.");
            return null;
        }

        // Check duplicate email
        try {
            PreparedStatement check = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
            check.setString(1, email);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                System.out.println("❌ Email already registered.");
                return null;
            }

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (name, email, password) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, hashPassword(password));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                User user = new User(keys.getInt(1), name.trim(), email.trim().toLowerCase(), password, 5.0, 0);
                System.out.println("✅ Registration successful! Welcome, " + name + "!");
                return user;
            }
        } catch (SQLException e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
        }
        return null;
    }

    public User login(String email, String password) {
        if (email == null || password == null) return null;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM users WHERE email = ? AND password = ?"
            );
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, hashPassword(password));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentUser = new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    password,
                    rs.getDouble("rating"),
                    rs.getInt("rating_count")
                );
                System.out.println("✅ Login successful! Welcome back, " + currentUser.getName() + "!");
                return currentUser;
            } else {
                System.out.println("❌ Invalid email or password.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Login error: " + e.getMessage());
        }
        return null;
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("👋 Goodbye, " + currentUser.getName() + "! You have been logged out.");
            currentUser = null;
        }
    }

    public boolean rateUser(int userId, int stars) {
        if (stars < 1 || stars > 5) {
            System.out.println("❌ Rating must be between 1 and 5.");
            return false;
        }
        try {
            // Get current rating
            PreparedStatement get = conn.prepareStatement("SELECT rating, rating_count FROM users WHERE id = ?");
            get.setInt(1, userId);
            ResultSet rs = get.executeQuery();
            if (rs.next()) {
                double curRating = rs.getDouble("rating");
                int curCount = rs.getInt("rating_count");
                double newRating = ((curRating * curCount) + stars) / (curCount + 1);
                PreparedStatement update = conn.prepareStatement(
                    "UPDATE users SET rating = ?, rating_count = ? WHERE id = ?"
                );
                update.setDouble(1, newRating);
                update.setInt(2, curCount + 1);
                update.setInt(3, userId);
                update.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("❌ Rating failed: " + e.getMessage());
        }
        return false;
    }

    public User getUserById(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    "",
                    rs.getDouble("rating"),
                    rs.getInt("rating_count")
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching user: " + e.getMessage());
        }
        return null;
    }

    public User getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }

    // Simple hash (in production, use BCrypt)
    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }
}
