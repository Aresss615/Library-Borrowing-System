package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents a borrow/return transaction between a user and a book.
 */
public class BorrowRecord {

    public enum Status { BORROWED, RETURNED, OVERDUE }

    /** Fine per day for overdue books */
    public static final BigDecimal FINE_PER_DAY = new BigDecimal("1.00");

    private int id;
    private int userId;
    private int bookId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BigDecimal fineAmount;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient fields for display (populated via JOINs)
    private String userName;
    private String bookTitle;

    // ── Constructors ───────────────────────────────────────────

    public BorrowRecord() {
        this.fineAmount = BigDecimal.ZERO;
    }

    public BorrowRecord(int userId, int bookId, LocalDate borrowDate, LocalDate dueDate) {
        this.userId     = userId;
        this.bookId     = bookId;
        this.borrowDate = borrowDate;
        this.dueDate    = dueDate;
        this.status     = Status.BORROWED;
        this.fineAmount = BigDecimal.ZERO;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public BigDecimal getFineAmount() { return fineAmount; }
    public void setFineAmount(BigDecimal fineAmount) { this.fineAmount = fineAmount; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    // ── Business logic ─────────────────────────────────────────

    /** Check if this record is overdue */
    public boolean isOverdue() {
        if (status == Status.RETURNED) return false;
        return LocalDate.now().isAfter(dueDate);
    }

    /** Calculate days overdue (0 if not overdue) */
    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        LocalDate endDate = (returnDate != null) ? returnDate : LocalDate.now();
        return ChronoUnit.DAYS.between(dueDate, endDate);
    }

    /** Calculate fine based on days overdue */
    public BigDecimal calculateFine() {
        long daysOverdue = getDaysOverdue();
        if (daysOverdue <= 0) return BigDecimal.ZERO;
        return FINE_PER_DAY.multiply(BigDecimal.valueOf(daysOverdue));
    }

    /** Status label for display */
    public String getStatusDisplay() {
        if (status == Status.RETURNED) return "Returned";
        if (isOverdue()) return "Overdue";
        return "Borrowed";
    }
}
