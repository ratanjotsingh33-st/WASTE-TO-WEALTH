package com.w2w.model;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private double rating;
    private int ratingCount;

    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.rating = 5.0;
        this.ratingCount = 0;
    }

    public User(int id, String name, String email, String password, double rating, int ratingCount) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.rating = rating;
        this.ratingCount = ratingCount;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    @Override
    public String toString() {
        return String.format("User[id=%d, name=%s, email=%s, rating=%.1f (%d reviews)]",
                id, name, email, rating, ratingCount);
    }
}
