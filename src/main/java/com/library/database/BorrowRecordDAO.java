package com.library.database;

import com.library.model.BorrowRecord;
import com.library.model.BorrowRecord.Status;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for BorrowRecord CRUD operations.
 */
public class BorrowRecordDAO {

    private final Connection conn;

    public BorrowRecordDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ── CRUD ───────────────────────────────────────────────────

    /** Get all borrow records with user and book names */
    public List<BorrowRecord> findAll() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = """
            SELECT br.*, u.full_name AS user_name, b.title AS book_title
            FROM borrow_records br
            JOIN users u ON br.user_id = u.id
            JOIN books b ON br.book_id = b.id
            ORDER BY br.borrow_date DESC
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /** Find records by user ID */
    public List<BorrowRecord> findByUserId(int userId) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = """
            SELECT br.*, u.full_name AS user_name, b.title AS book_title
            FROM borrow_records br
            JOIN users u ON br.user_id = u.id
            JOIN books b ON br.book_id = b.id
            WHERE br.user_id = ?
            ORDER BY br.borrow_date DESC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /** Find currently borrowed (active) records */
    public List<BorrowRecord> findBorrowed() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = """
            SELECT br.*, u.full_name AS user_name, b.title AS book_title
            FROM borrow_records br
            JOIN users u ON br.user_id = u.id
            JOIN books b ON br.book_id = b.id
            WHERE br.status IN ('BORROWED', 'OVERDUE')
            ORDER BY br.due_date ASC
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /** Find returned records */
    public List<BorrowRecord> findReturned() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = """
            SELECT br.*, u.full_name AS user_name, b.title AS book_title
            FROM borrow_records br
            JOIN users u ON br.user_id = u.id
            JOIN books b ON br.book_id = b.id
            WHERE br.status = 'RETURNED'
            ORDER BY br.return_date DESC
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /** Find overdue records */
    public List<BorrowRecord> findOverdue() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = """
            SELECT br.*, u.full_name AS user_name, b.title AS book_title
            FROM borrow_records br
            JOIN users u ON br.user_id = u.id
            JOIN books b ON br.book_id = b.id
            WHERE br.status != 'RETURNED' AND br.due_date < CURDATE()
            ORDER BY br.due_date ASC
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /** Insert a new borrow record */
    public boolean insert(BorrowRecord record) {
        String sql = "INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getUserId());
            ps.setInt(2, record.getBookId());
            ps.setDate(3, Date.valueOf(record.getBorrowDate()));
            ps.setDate(4, Date.valueOf(record.getDueDate()));
            ps.setString(5, record.getStatus().name());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    record.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Mark a record as returned */
    public boolean returnBook(int recordId, BigDecimal fine) {
        String sql = "UPDATE borrow_records SET return_date = ?, fine_amount = ?, status = 'RETURNED' WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setBigDecimal(2, fine);
            ps.setInt(3, recordId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Update overdue statuses in the database */
    public int updateOverdueStatuses() {
        String sql = "UPDATE borrow_records SET status = 'OVERDUE' WHERE status = 'BORROWED' AND due_date < CURDATE()";
        try (Statement st = conn.createStatement()) {
            return st.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Check if a specific user already has a specific book borrowed */
    public boolean hasActiveBorrow(int userId, int bookId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND book_id = ? AND status IN ('BORROWED', 'OVERDUE')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Statistics ──────────────────────────────────────────────

    /** Count currently borrowed books */
    public int countBorrowed() {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE status IN ('BORROWED', 'OVERDUE')";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Count overdue books */
    public int countOverdue() {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE status != 'RETURNED' AND due_date < CURDATE()";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Count returned books */
    public int countReturned() {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE status = 'RETURNED'";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Search borrow records by user name or book title */
    public List<BorrowRecord> search(String keyword) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = """
            SELECT br.*, u.full_name AS user_name, b.title AS book_title
            FROM borrow_records br
            JOIN users u ON br.user_id = u.id
            JOIN books b ON br.book_id = b.id
            WHERE u.full_name LIKE ? OR b.title LIKE ?
            ORDER BY br.borrow_date DESC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /** Count active (non-returned) borrows for a specific user */
    public int countActiveByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND status IN ('BORROWED', 'OVERDUE')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Search active borrow records by user name, username, book title, or record ID */
    public List<BorrowRecord> searchActive(String keyword) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = """
            SELECT br.*, u.full_name AS user_name, b.title AS book_title
            FROM borrow_records br
            JOIN users u ON br.user_id = u.id
            JOIN books b ON br.book_id = b.id
            WHERE br.status IN ('BORROWED', 'OVERDUE')
            AND (u.full_name LIKE ? OR b.title LIKE ? OR CAST(br.id AS CHAR) LIKE ? OR u.username LIKE ?)
            ORDER BY br.due_date ASC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /** Find the most recent borrow records (for dashboard activity feed) */
    public List<BorrowRecord> findRecent(int limit) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = """
            SELECT br.*, u.full_name AS user_name, b.title AS book_title
            FROM borrow_records br
            JOIN users u ON br.user_id = u.id
            JOIN books b ON br.book_id = b.id
            ORDER BY br.updated_at DESC
            LIMIT ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    // ── Mapper ─────────────────────────────────────────────────

    private BorrowRecord mapRecord(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setId(rs.getInt("id"));
        record.setUserId(rs.getInt("user_id"));
        record.setBookId(rs.getInt("book_id"));
        record.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
        record.setDueDate(rs.getDate("due_date").toLocalDate());
        Date returnDate = rs.getDate("return_date");
        if (returnDate != null) record.setReturnDate(returnDate.toLocalDate());
        record.setFineAmount(rs.getBigDecimal("fine_amount"));
        record.setStatus(Status.valueOf(rs.getString("status")));
        // Transient display fields
        try {
            record.setUserName(rs.getString("user_name"));
            record.setBookTitle(rs.getString("book_title"));
        } catch (SQLException ignored) {
            // These columns may not be present in all queries
        }
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) record.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) record.setUpdatedAt(updated.toLocalDateTime());
        return record;
    }
}
