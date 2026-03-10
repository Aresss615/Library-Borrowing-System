package com.library.utils;

/**
 * Form validation utilities.
 * Returns error messages or null if valid.
 */
public class ValidationHelper {

    /** Check if a string is null or blank */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** Validate required field */
    public static String validateRequired(String value, String fieldName) {
        if (isBlank(value)) {
            return fieldName + " is required.";
        }
        return null;
    }

    /** Validate email format */
    public static String validateEmail(String email) {
        if (isBlank(email)) return null; // email is optional
        if (!email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return "Invalid email format.";
        }
        return null;
    }

    /** Validate phone format */
    public static String validatePhone(String phone) {
        if (isBlank(phone)) return null; // phone is optional
        if (!phone.matches("^[\\d+\\-() ]{7,20}$")) {
            return "Invalid phone format.";
        }
        return null;
    }

    /** Validate that a string is a positive integer */
    public static String validatePositiveInt(String value, String fieldName) {
        if (isBlank(value)) {
            return fieldName + " is required.";
        }
        try {
            int num = Integer.parseInt(value.trim());
            if (num <= 0) return fieldName + " must be a positive number.";
        } catch (NumberFormatException e) {
            return fieldName + " must be a valid number.";
        }
        return null;
    }

    /** Validate year */
    public static String validateYear(String value) {
        if (isBlank(value)) return null; // year is optional
        try {
            int year = Integer.parseInt(value.trim());
            if (year < 1000 || year > 2100) return "Year must be between 1000 and 2100.";
        } catch (NumberFormatException e) {
            return "Year must be a valid number.";
        }
        return null;
    }

    /** Validate username: alphanumeric, dots, underscores, 3-50 chars */
    public static String validateUsername(String username) {
        if (isBlank(username)) return "Username is required.";
        if (!username.matches("^[a-zA-Z0-9._]{3,50}$")) {
            return "Username must be 3-50 characters (letters, numbers, dots, underscores).";
        }
        return null;
    }

    /** Validate password: at least 6 characters */
    public static String validatePassword(String password) {
        if (isBlank(password)) return "Password is required.";
        if (password.length() < 6) return "Password must be at least 6 characters.";
        return null;
    }
}
