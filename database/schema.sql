-- ============================================================
-- Library Borrowing System - Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

-- ============================================================
-- Users table: stores admin and student accounts
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100),
    phone       VARCHAR(20),
    role        ENUM('ADMIN', 'STUDENT') NOT NULL DEFAULT 'STUDENT',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Books table: catalog of all library books
-- ============================================================
CREATE TABLE IF NOT EXISTS books (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    isbn            VARCHAR(20)  UNIQUE,
    title           VARCHAR(255) NOT NULL,
    author          VARCHAR(150) NOT NULL,
    publisher       VARCHAR(150),
    year_published  INT,
    category        VARCHAR(100),
    total_copies    INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Borrow records table: tracks every borrow/return transaction
-- ============================================================
CREATE TABLE IF NOT EXISTS borrow_records (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    book_id         INT NOT NULL,
    borrow_date     DATE NOT NULL,
    due_date        DATE NOT NULL,
    return_date     DATE DEFAULT NULL,
    fine_amount     DECIMAL(10, 2) DEFAULT 0.00,
    status          ENUM('BORROWED', 'RETURNED', 'OVERDUE') NOT NULL DEFAULT 'BORROWED',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Indexes for performance
-- ============================================================
CREATE INDEX idx_borrow_user   ON borrow_records(user_id);
CREATE INDEX idx_borrow_book   ON borrow_records(book_id);
CREATE INDEX idx_borrow_status ON borrow_records(status);
CREATE INDEX idx_book_title    ON books(title);
CREATE INDEX idx_book_author   ON books(author);

-- ============================================================
-- Sample Data
-- ============================================================

-- Admin account (password: admin123)
INSERT INTO users (username, password, full_name, email, phone, role) VALUES
('admin',    'admin123',    'System Administrator', 'admin@library.com',   '555-0100', 'ADMIN'),
('librarian','librarian123','Jane Librarian',       'jane@library.com',    '555-0101', 'ADMIN');

-- Student accounts (password: student123)
INSERT INTO users (username, password, full_name, email, phone, role) VALUES
('john.doe',     'student123', 'John Doe',      'john@university.edu',    '555-0201', 'STUDENT'),
('sarah.smith',  'student123', 'Sarah Smith',   'sarah@university.edu',   '555-0202', 'STUDENT'),
('mike.johnson', 'student123', 'Mike Johnson',  'mike@university.edu',    '555-0203', 'STUDENT'),
('emily.wilson', 'student123', 'Emily Wilson',  'emily@university.edu',   '555-0204', 'STUDENT'),
('david.brown',  'student123', 'David Brown',   'david@university.edu',   '555-0205', 'STUDENT'),
('lisa.wang',    'student123', 'Lisa Wang',     'lisa@university.edu',    '555-0206', 'STUDENT');

-- Sample books
INSERT INTO books (isbn, title, author, publisher, year_published, category, total_copies, available_copies) VALUES
('978-0-06-112008-4', 'To Kill a Mockingbird',           'Harper Lee',           'J.B. Lippincott & Co.',  1960, 'Fiction',           5, 3),
('978-0-452-28423-4', '1984',                            'George Orwell',        'Secker & Warburg',       1949, 'Dystopian Fiction', 4, 2),
('978-0-7432-7356-5', 'The Great Gatsby',                'F. Scott Fitzgerald',  'Charles Scribners Sons', 1925, 'Classic Fiction',   3, 1),
('978-0-316-76948-0', 'The Catcher in the Rye',          'J.D. Salinger',        'Little, Brown',          1951, 'Fiction',           4, 4),
('978-0-14-028329-7', 'The Grapes of Wrath',             'John Steinbeck',       'The Viking Press',       1939, 'Fiction',           2, 2),
('978-0-06-093546-7', 'To Kill a Mockingbird - Special', 'Harper Lee',           'HarperCollins',          2002, 'Fiction',           2, 2),
('978-0-14-118776-1', 'Of Mice and Men',                 'John Steinbeck',       'Covici Friede',          1937, 'Fiction',           3, 3),
('978-0-06-120008-9', 'Brave New World',                 'Aldous Huxley',        'Chatto & Windus',        1932, 'Dystopian Fiction', 3, 2),
('978-0-261-10325-4', 'The Lord of the Rings',           'J.R.R. Tolkien',       'Allen & Unwin',          1954, 'Fantasy',           5, 3),
('978-0-590-35340-3', 'Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling',  'Bloomsbury Publishing',  1997, 'Fantasy',           6, 4),
('978-0-06-025492-6', 'Where the Wild Things Are',       'Maurice Sendak',       'Harper & Row',           1963, 'Children',          3, 3),
('978-0-375-82681-5', 'The Kite Runner',                 'Khaled Hosseini',      'Riverhead Books',        2003, 'Fiction',           4, 3),
('978-0-13-468599-1', 'The C Programming Language',      'Brian Kernighan',      'Prentice Hall',          1978, 'Technology',        3, 2),
('978-0-201-63361-0', 'Design Patterns',                 'Gang of Four',         'Addison-Wesley',         1994, 'Technology',        2, 1),
('978-0-13-235088-4', 'Clean Code',                      'Robert C. Martin',     'Prentice Hall',          2008, 'Technology',        4, 3),
('978-0-596-00712-6', 'Head First Design Patterns',      'Eric Freeman',         'O''Reilly Media',        2004, 'Technology',        3, 2),
('978-0-321-12521-7', 'Domain-Driven Design',            'Eric Evans',           'Addison-Wesley',         2003, 'Technology',        2, 2),
('978-0-13-468599-2', 'Introduction to Algorithms',      'Thomas H. Cormen',     'MIT Press',              2009, 'Technology',        3, 3),
('978-0-06-093546-8', 'Sapiens',                         'Yuval Noah Harari',    'Harper',                 2015, 'Non-Fiction',       5, 4),
('978-1-5011-1168-3', 'Educated',                        'Tara Westover',        'Random House',           2018, 'Non-Fiction',       3, 2);

-- Sample borrow records
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, return_date, fine_amount, status) VALUES
(3, 1, '2026-02-01', '2026-02-15', '2026-02-14', 0.00, 'RETURNED'),
(3, 2, '2026-02-20', '2026-03-06', NULL,         0.00, 'OVERDUE'),
(4, 3, '2026-02-25', '2026-03-11', NULL,         0.00, 'BORROWED'),
(4, 9, '2026-02-10', '2026-02-24', '2026-02-23', 0.00, 'RETURNED'),
(5, 10,'2026-03-01', '2026-03-15', NULL,         0.00, 'BORROWED'),
(5, 1, '2026-02-15', '2026-03-01', NULL,         0.00, 'OVERDUE'),
(6, 8, '2026-03-05', '2026-03-19', NULL,         0.00, 'BORROWED'),
(7, 14,'2026-02-20', '2026-03-06', NULL,         0.00, 'OVERDUE'),
(7, 15,'2026-03-08', '2026-03-22', NULL,         0.00, 'BORROWED'),
(8, 2, '2026-02-28', '2026-03-14', NULL,         0.00, 'BORROWED'),
(8, 19,'2026-03-02', '2026-03-16', NULL,         0.00, 'BORROWED');
