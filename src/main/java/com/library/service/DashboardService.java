package com.library.service;

import com.library.database.BookDAO;
import com.library.database.BorrowRecordDAO;
import com.library.database.UserDAO;
import com.library.model.DashboardStats;

/**
 * Service layer for dashboard statistics.
 * Aggregates data from multiple DAOs.
 */
public class DashboardService {

    private final BookDAO bookDAO;
    private final UserDAO userDAO;
    private final BorrowRecordDAO borrowRecordDAO;

    public DashboardService() {
        this.bookDAO         = new BookDAO();
        this.userDAO         = new UserDAO();
        this.borrowRecordDAO = new BorrowRecordDAO();
    }

    /**
     * Collects all dashboard statistics in one call.
     * Also updates overdue statuses in the database.
     */
    public DashboardStats getStats() {
        // First, auto-detect and update overdue records
        borrowRecordDAO.updateOverdueStatuses();

        DashboardStats stats = new DashboardStats();
        stats.setTotalBooks(bookDAO.countAll());
        stats.setTotalBorrowed(borrowRecordDAO.countBorrowed());
        stats.setTotalUsers(userDAO.countStudents());
        stats.setOverdueBooks(borrowRecordDAO.countOverdue());
        stats.setTotalAvailable(bookDAO.countAvailable());
        stats.setTotalReturned(borrowRecordDAO.countReturned());
        return stats;
    }
}
