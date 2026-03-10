package com.library.model;

import java.time.LocalDateTime;

/**
 * Represents a book in the library catalog.
 */
public class Book {

    private int id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int yearPublished;
    private String category;
    private int totalCopies;
    private int availableCopies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ───────────────────────────────────────────

    public Book() {}

    public Book(String isbn, String title, String author, String publisher,
                int yearPublished, String category, int totalCopies) {
        this.isbn            = isbn;
        this.title           = title;
        this.author          = author;
        this.publisher       = publisher;
        this.yearPublished   = yearPublished;
        this.category        = category;
        this.totalCopies     = totalCopies;
        this.availableCopies = totalCopies;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public int getYearPublished() { return yearPublished; }
    public void setYearPublished(int yearPublished) { this.yearPublished = yearPublished; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /** Number of copies currently borrowed */
    public int getBorrowedCopies() {
        return totalCopies - availableCopies;
    }

    @Override
    public String toString() {
        return title + " by " + author;
    }
}
