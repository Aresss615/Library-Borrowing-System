package com.library.database;

import com.library.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Book CRUD operations.
 */
public class BookDAO {

    private final Connection conn;

    public BookDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ── CRUD ───────────────────────────────────────────────────

    /** Get all books */
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY title";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    /** Find book by ID */
    public Book findById(int id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapBook(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Search books by title, author, ISBN, or category */
    public List<Book> search(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? OR category LIKE ? ORDER BY title";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                books.add(mapBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    /** Insert a new book */
    public boolean insert(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, publisher, year_published, category, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());
            ps.setInt(5, book.getYearPublished());
            ps.setString(6, book.getCategory());
            ps.setInt(7, book.getTotalCopies());
            ps.setInt(8, book.getAvailableCopies());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    book.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Update an existing book */
    public boolean update(Book book) {
        String sql = "UPDATE books SET isbn = ?, title = ?, author = ?, publisher = ?, year_published = ?, category = ?, total_copies = ?, available_copies = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());
            ps.setInt(5, book.getYearPublished());
            ps.setString(6, book.getCategory());
            ps.setInt(7, book.getTotalCopies());
            ps.setInt(8, book.getAvailableCopies());
            ps.setInt(9, book.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Delete a book by ID */
    public boolean delete(int id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Decrement available copies when borrowing */
    public boolean decrementAvailable(int bookId) {
        String sql = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND available_copies > 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Increment available copies when returning */
    public boolean incrementAvailable(int bookId) {
        String sql = "UPDATE books SET available_copies = available_copies + 1 WHERE id = ? AND available_copies < total_copies";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Statistics ──────────────────────────────────────────────

    /** Count total books (unique titles) */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM books";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Count total available copies across all books */
    public int countAvailable() {
        String sql = "SELECT COALESCE(SUM(available_copies), 0) FROM books";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Search books with available copies, filtered by keyword */
    public List<Book> searchAvailable(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE available_copies > 0 AND (title LIKE ? OR author LIKE ? OR isbn LIKE ? OR category LIKE ?) ORDER BY title";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                books.add(mapBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    /** Find low-stock books (at most 1 copy available out of 2+ total) */
    public List<Book> findLowStock() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE available_copies <= 1 AND total_copies > 1 ORDER BY available_copies ASC, title";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // ── Mapper ─────────────────────────────────────────────────

    private Book mapBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setYearPublished(rs.getInt("year_published"));
        book.setCategory(rs.getString("category"));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) book.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) book.setUpdatedAt(updated.toLocalDateTime());
        return book;
    }
}
