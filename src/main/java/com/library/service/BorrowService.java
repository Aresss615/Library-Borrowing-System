package com.library.service;

import com.library.database.BookDAO;
import com.library.database.BorrowRecordDAO;
import com.library.model.Book;
import com.library.model.BorrowRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for borrowing operations.
 * Handles business logic around borrowing/returning books.
 */
public class BorrowService {

    private static final int DEFAULT_BORROW_DAYS = 14; // 2 weeks

    private final BorrowRecordDAO borrowRecordDAO;
    private final BookDAO bookDAO;

    public BorrowService() {
        this.borrowRecordDAO = new BorrowRecordDAO();
        this.bookDAO         = new BookDAO();
    }

    /**
     * Borrow a book for a user.
     * Validates availability and prevents duplicate borrows.
     *
     * @return null on success, or an error message string
     */
    public String borrowBook(int userId, int bookId) {
        // Check if book exists and has copies available
        Book book = bookDAO.findById(bookId);
        if (book == null) return "Book not found.";
        if (book.getAvailableCopies() <= 0) return "No copies available for this book.";

        // Check if user already has this book borrowed
        if (borrowRecordDAO.hasActiveBorrow(userId, bookId)) {
            return "User already has this book borrowed.";
        }

        // Create borrow record
        LocalDate today   = LocalDate.now();
        LocalDate dueDate = today.plusDays(DEFAULT_BORROW_DAYS);
        BorrowRecord record = new BorrowRecord(userId, bookId, today, dueDate);

        // Decrement available copies
        if (!bookDAO.decrementAvailable(bookId)) {
            return "Failed to update book availability.";
        }

        // Insert record
        if (!borrowRecordDAO.insert(record)) {
            bookDAO.incrementAvailable(bookId); // rollback
            return "Failed to create borrow record.";
        }

        return null; // success
    }

    /**
     * Return a borrowed book.
     * Calculates fine if overdue.
     *
     * @return null on success, or an error message string
     */
    public String returnBook(BorrowRecord record) {
        if (record == null) return "Record not found.";
        if (record.getStatus() == BorrowRecord.Status.RETURNED) {
            return "Book has already been returned.";
        }

        // Calculate fine
        BigDecimal fine = record.calculateFine();

        // Update borrow record
        if (!borrowRecordDAO.returnBook(record.getId(), fine)) {
            return "Failed to update borrow record.";
        }

        // Increment available copies
        bookDAO.incrementAvailable(record.getBookId());

        return null; // success
    }

    /** Get all borrow records */
    public List<BorrowRecord> getAllRecords() {
        borrowRecordDAO.updateOverdueStatuses();
        return borrowRecordDAO.findAll();
    }

    /** Get records by user */
    public List<BorrowRecord> getRecordsByUser(int userId) {
        return borrowRecordDAO.findByUserId(userId);
    }

    /** Get currently borrowed records */
    public List<BorrowRecord> getBorrowedRecords() {
        borrowRecordDAO.updateOverdueStatuses();
        return borrowRecordDAO.findBorrowed();
    }

    /** Get returned records */
    public List<BorrowRecord> getReturnedRecords() {
        return borrowRecordDAO.findReturned();
    }

    /** Get overdue records */
    public List<BorrowRecord> getOverdueRecords() {
        borrowRecordDAO.updateOverdueStatuses();
        return borrowRecordDAO.findOverdue();
    }

    /** Search records */
    public List<BorrowRecord> searchRecords(String keyword) {
        return borrowRecordDAO.search(keyword);
    }
}
