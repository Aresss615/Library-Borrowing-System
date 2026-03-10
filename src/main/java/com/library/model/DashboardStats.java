package com.library.model;

/**
 * Holds dashboard statistics for the overview panel.
 */
public class DashboardStats {

    private int totalBooks;
    private int totalBorrowed;
    private int totalUsers;
    private int overdueBooks;
    private int totalAvailable;
    private int totalReturned;

    public int getTotalBooks() { return totalBooks; }
    public void setTotalBooks(int totalBooks) { this.totalBooks = totalBooks; }

    public int getTotalBorrowed() { return totalBorrowed; }
    public void setTotalBorrowed(int totalBorrowed) { this.totalBorrowed = totalBorrowed; }

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    public int getOverdueBooks() { return overdueBooks; }
    public void setOverdueBooks(int overdueBooks) { this.overdueBooks = overdueBooks; }

    public int getTotalAvailable() { return totalAvailable; }
    public void setTotalAvailable(int totalAvailable) { this.totalAvailable = totalAvailable; }

    public int getTotalReturned() { return totalReturned; }
    public void setTotalReturned(int totalReturned) { this.totalReturned = totalReturned; }
}
