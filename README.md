# Library Borrowing System

A modern Library Borrowing System built with Java, JavaFX, and MySQL — featuring an Apple-inspired clean UI design.

![Java](https://img.shields.io/badge/Java-17+-orange) ![JavaFX](https://img.shields.io/badge/JavaFX-21-blue) ![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)

---

## Features

- **User Authentication** — Admin and Student login
- **Book Management** — Add, edit, delete, search books with copy tracking
- **Borrowing System** — Borrow/return books with due dates and history
- **User Management** — Full CRUD with borrowing history per user
- **Dashboard** — Statistics cards with total books, borrowed, users, overdue
- **Overdue Tracking** — Automatic detection with fine calculation ($1/day)
- **Reports** — Borrowed, returned, and overdue reports with filtering

---

## Prerequisites

1. **Java JDK 17+** — [Download from Oracle](https://www.oracle.com/java/technologies/downloads/) or [Adoptium](https://adoptium.net/)
2. **Apache Maven 3.8+** — [Download Maven](https://maven.apache.org/download.cgi)
3. **MySQL 8.0+** — [Download MySQL](https://dev.mysql.com/downloads/mysql/)
4. **VS Code** with these extensions:
   - Extension Pack for Java
   - Maven for Java

---

## Setup Instructions

### Step 1: Set Up the Database

1. Start your MySQL server

2. Open a MySQL client (MySQL Workbench, command line, etc.)

3. Run the SQL schema file:
   ```sql
   source database/schema.sql
   ```
   Or copy-paste the contents of `database/schema.sql` into your MySQL client.

   This will:
   - Create the `library_db` database
   - Create the `users`, `books`, and `borrow_records` tables
   - Insert sample data (admin, students, books, borrow records)

### Step 2: Configure Database Connection

Edit the database credentials in:
```
src/main/java/com/library/database/DatabaseConnection.java
```

Update these constants to match your MySQL setup:
```java
private static final String URL      = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String USER     = "root";
private static final String PASSWORD = "";  // ← Change this to your MySQL password
```

### Step 3: Build and Run

#### Option A: Using Maven (Recommended)

```bash
# Navigate to project directory
cd Library-Borrowing-System

# Compile the project
mvn clean compile

# Run the application
mvn javafx:run
```

#### Option B: Using VS Code

1. Open the project folder in VS Code
2. Wait for Java extensions to recognize the Maven project
3. Open `src/main/java/com/library/LibraryApp.java`
4. Click the **Run** button (▶) above `public static void main`
5. Or press `F5` to run in debug mode

---

## Login Credentials

| Role    | Username     | Password      |
|---------|-------------|---------------|
| Admin   | `admin`     | `admin123`    |
| Admin   | `librarian` | `librarian123`|
| Student | `john.doe`  | `student123`  |
| Student | `sarah.smith`| `student123` |
| Student | `mike.johnson`| `student123`|

---

## Project Structure

```
Library-Borrowing-System/
├── pom.xml                          # Maven config with JavaFX + MySQL deps
├── database/
│   └── schema.sql                   # Full DB schema + sample data
└── src/main/
    ├── java/
    │   ├── module-info.java         # Java module descriptor
    │   └── com/library/
    │       ├── LibraryApp.java      # Application entry point
    │       ├── model/
    │       │   ├── User.java        # User entity
    │       │   ├── Book.java        # Book entity
    │       │   ├── BorrowRecord.java# Borrow transaction entity
    │       │   └── DashboardStats.java # Dashboard statistics DTO
    │       ├── database/
    │       │   ├── DatabaseConnection.java  # MySQL connection singleton
    │       │   ├── UserDAO.java     # User data access
    │       │   ├── BookDAO.java     # Book data access
    │       │   └── BorrowRecordDAO.java # Borrow record data access
    │       ├── service/
    │       │   ├── DashboardService.java # Dashboard statistics
    │       │   └── BorrowService.java    # Borrow/return logic
    │       ├── controller/
    │       │   ├── LoginController.java  # Login screen controller
    │       │   └── MainController.java   # Main layout + navigation
    │       ├── view/
    │       │   ├── DashboardView.java    # Dashboard stats cards
    │       │   ├── BookView.java         # Book management
    │       │   ├── UserView.java         # User management
    │       │   ├── BorrowView.java       # Borrowing management
    │       │   ├── OverdueView.java      # Overdue tracking
    │       │   └── ReportsView.java      # Reports with tabs
    │       └── utils/
    │           ├── AlertHelper.java      # Styled dialog alerts
    │           ├── ValidationHelper.java # Form validation
    │           └── SessionManager.java   # Auth session singleton
    └── resources/
        ├── fxml/
        │   ├── Login.fxml               # Login screen layout
        │   └── MainLayout.fxml          # Main app layout + sidebar
        └── styles/
            └── style.css                # Apple-inspired CSS theme
```

---

## Architecture

- **Model** — Plain Java objects (User, Book, BorrowRecord, DashboardStats)
- **Database / DAO** — Data Access Objects handle all SQL queries via JDBC
- **Service** — Business logic layer (BorrowService, DashboardService)
- **Controller** — FXML controllers for Login and Main Layout
- **View** — Programmatic JavaFX views for each page (Dashboard, Books, etc.)
- **Utils** — Helpers for alerts, validation, and session management

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Run `mvn clean compile` to download MySQL driver |
| Database connection failed | Check MySQL is running and credentials are correct |
| JavaFX not found | Ensure JDK 17+ is set and Maven has downloaded JavaFX deps |
| FXML load error | Rebuild with `mvn clean compile` |
| Module access errors | The `module-info.java` handles all `opens`/`exports` |

---

## Design Notes

The UI follows Apple's Human Interface Guidelines:
- **Clean typography** with system fonts
- **Rounded corners** (16px radius) on all panels and cards
- **Subtle shadows** for depth without heaviness
- **Color-coded accents** — blue (primary), green (success), red (danger), orange (warning)
- **Generous whitespace** and consistent spacing
- **Status badges** with soft colored backgrounds
- **Thin scrollbars** and modern table styling
