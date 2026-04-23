package com.w2w.util;

public class Validator {

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() >= 2;
    }

    public static boolean isValidDate(String date) {
        if (date == null || date.isEmpty()) return true; // optional
        return date.matches("\\d{4}-\\d{2}-\\d{2}");
    }
}
