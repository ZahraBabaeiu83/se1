package Library;

import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class Database {

    public static final String DATA_DIR = "data";
    public static final String DB_FILE_NAME = "library.db";

    private static final Database INSTANCE = new Database();

    private List<User> users = new ArrayList<>();
    private List<Book> books = new ArrayList<>();
    private List<LoanRequest> requests = new ArrayList<>();
    private List<Loan> loans = new ArrayList<>();

    private transient boolean firstRun = false;
    private transient boolean loaded = false;

    private Database() {
    }

    public static Database getInstance() {
        return INSTANCE;
    }

    public void load() {
        if (loaded) {
            return;
        }
        File dir = new File(DATA_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("خطا در ایجاد پوشه داده: " + dir.getAbsolutePath());
        }

        File dbFile = new File(DATA_DIR, DB_FILE_NAME);
        boolean freshDb = !dbFile.exists();

        try (Connection connection = connect()) {
            createTables(connection);
            if (freshDb && !migrateFromDatFiles()) {
                users.add(new Manager(nextUserId(), "admin", "admin123", "مدیر سیستم"));
                firstRun = true;
                save();
            }
            loadAll(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("خطا در اتصال به پایگاه داده SQLite: " + e.getMessage(), e);
        }
        loaded = true;
    }

    private Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("درایور SQLite (sqlite-jdbc) در classpath یافت نشد.", e);
        }
        return DriverManager.getConnection("jdbc:sqlite:" + DATA_DIR + File.separator + DB_FILE_NAME);
    }

    private void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id             INTEGER PRIMARY KEY,
                        role           TEXT    NOT NULL,
                        username       TEXT    NOT NULL UNIQUE,
                        password       TEXT    NOT NULL,
                        full_name      TEXT    NOT NULL DEFAULT '',
                        active         INTEGER NOT NULL DEFAULT 1,
                        student_number TEXT    NOT NULL DEFAULT ''
                    )""");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS books (
                        id             INTEGER PRIMARY KEY,
                        title          TEXT    NOT NULL,
                        author         TEXT    NOT NULL,
                        publish_year   INTEGER NOT NULL,
                        registered_by  TEXT    NOT NULL
                    )""");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS loan_requests (
                        id                 INTEGER PRIMARY KEY,
                        student_id         INTEGER NOT NULL,
                        book_id            INTEGER NOT NULL,
                        start_date         TEXT    NOT NULL,
                        end_date           TEXT    NOT NULL,
                        request_time       TEXT    NOT NULL,
                        status             TEXT    NOT NULL,
                        reviewer_username  TEXT,
                        review_time        TEXT
                    )""");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS loans (
                        id                       INTEGER PRIMARY KEY,
                        request_id               INTEGER NOT NULL,
                        student_id               INTEGER NOT NULL,
                        book_id                  INTEGER NOT NULL,
                        loan_employee_username   TEXT    NOT NULL,
                        borrow_time              TEXT    NOT NULL,
                        due_date                 TEXT    NOT NULL,
                        return_time              TEXT,
                        return_employee_username TEXT
                    )""");
        }
    }

    /**
     * یک‌بارمصرف: اگر پایگاه داده تازه ساخته شده ولی فایل‌های قدیمی .dat موجود باشند،
     * داده‌ها از آن فایل‌ها وارد پایگاه داده SQLite می‌شوند.
     *
     * @return true اگر داده‌ای وارد شد
     */
    private boolean migrateFromDatFiles() {
        List<User> datUsers = (List<User>) readDatObject("users.dat");
        List<Book> datBooks = (List<Book>) readDatObject("books.dat");
        List<LoanRequest> datRequests = (List<LoanRequest>) readDatObject("requests.dat");
        List<Loan> datLoans = (List<Loan>) readDatObject("loans.dat");
        if (datUsers == null && datBooks == null && datRequests == null && datLoans == null) {
            return false;
        }
        users = datUsers != null ? datUsers : new ArrayList<>();
        books = datBooks != null ? datBooks : new ArrayList<>();
        requests = datRequests != null ? datRequests : new ArrayList<>();
        loans = datLoans != null ? datLoans : new ArrayList<>();
        save();
        System.out.println("داده‌های قدیمی از فایل‌های .dat به پایگاه داده SQLite منتقل شد.");
        return true;
    }

    @SuppressWarnings("unchecked")
    private Object readDatObject(String fileName) {
        File file = new File(DATA_DIR, fileName);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            return in.readObject();
        } catch (Exception e) {
            System.err.println("هشدار: خواندن فایل " + fileName + " ناموفق بود (" + e.getMessage() + ")");
            return null;
        }
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public void save() {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM loans");
                statement.executeUpdate("DELETE FROM loan_requests");
                statement.executeUpdate("DELETE FROM books");
                statement.executeUpdate("DELETE FROM users");
            }
            insertUsers(connection);
            insertBooks(connection);
            insertRequests(connection);
            insertLoans(connection);
            connection.commit();
        } catch (SQLException e) {
            System.err.println("خطا در ذخیره‌سازی در پایگاه داده: " + e.getMessage());
        }
    }

    private void insertUsers(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (id, role, username, password, full_name, active, student_number) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            for (User user : users) {
                String role;
                int active;
                String studentNumber = "";
                if (user instanceof Manager) {
                    role = "MANAGER";
                    active = 1;
                } else if (user instanceof Employee) {
                    role = "EMPLOYEE";
                    active = 1;
                } else if (user instanceof Student) {
                    role = "STUDENT";
                    active = ((Student) user).isActive() ? 1 : 0;
                    studentNumber = ((Student) user).getStudentNumber();
                } else {
                    continue;
                }
                ps.setInt(1, user.getId());
                ps.setString(2, role);
                ps.setString(3, user.getUsername());
                ps.setString(4, user.getPasswordForStorage());
                ps.setString(5, user.getFullName());
                ps.setInt(6, active);
                ps.setString(7, studentNumber);
                ps.executeUpdate();
            }
        }
    }

    private void insertBooks(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO books (id, title, author, publish_year, registered_by) VALUES (?, ?, ?, ?, ?)")) {
            for (Book book : books) {
                ps.setInt(1, book.getId());
                ps.setString(2, book.getTitle());
                ps.setString(3, book.getAuthor());
                ps.setInt(4, book.getPublishYear());
                ps.setString(5, book.getRegisteredBy());
                ps.executeUpdate();
            }
        }
    }

    private void insertRequests(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO loan_requests (id, student_id, book_id, start_date, end_date, request_time, "
                        + "status, reviewer_username, review_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            for (LoanRequest request : requests) {
                ps.setInt(1, request.getId());
                ps.setInt(2, request.getStudentId());
                ps.setInt(3, request.getBookId());
                ps.setString(4, request.getStartDate().toString());
                ps.setString(5, request.getEndDate().toString());
                ps.setString(6, request.getRequestTime().toString());
                ps.setString(7, request.getStatus().name());
                ps.setString(8, request.getReviewerUsername());
                ps.setString(9, request.getReviewTime() == null ? null : request.getReviewTime().toString());
                ps.executeUpdate();
            }
        }
    }

    private void insertLoans(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO loans (id, request_id, student_id, book_id, loan_employee_username, borrow_time, "
                        + "due_date, return_time, return_employee_username) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            for (Loan loan : loans) {
                ps.setInt(1, loan.getId());
                ps.setInt(2, loan.getRequestId());
                ps.setInt(3, loan.getStudentId());
                ps.setInt(4, loan.getBookId());
                ps.setString(5, loan.getLoanEmployeeUsername());
                ps.setString(6, loan.getBorrowTime().toString());
                ps.setString(7, loan.getDueDate().toString());
                ps.setString(8, loan.getReturnTime() == null ? null : loan.getReturnTime().toString());
                ps.setString(9, loan.getReturnEmployeeUsername());
                ps.executeUpdate();
            }
        }
    }

    private void loadAll(Connection connection) throws SQLException {
        users = new ArrayList<>();
        books = new ArrayList<>();
        requests = new ArrayList<>();
        loans = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM users ORDER BY id")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("role");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String fullName = rs.getString("full_name");
                switch (role) {
                    case "MANAGER" -> users.add(new Manager(id, username, password, fullName));
                    case "EMPLOYEE" -> users.add(new Employee(id, username, password, fullName));
                    case "STUDENT" -> {
                        Student student = new Student(id, username, password, fullName);
                        student.setActive(rs.getInt("active") != 0);
                        student.setStudentNumber(rs.getString("student_number"));
                        users.add(student);
                    }
                    default -> System.err.println("نقش ناشناخته در پایگاه داده: " + role);
                }
            }
        }

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM books ORDER BY id")) {
            while (rs.next()) {
                books.add(new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"),
                        rs.getInt("publish_year"), rs.getString("registered_by")));
            }
        }

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM loan_requests ORDER BY id")) {
            while (rs.next()) {
                LoanRequest request = new LoanRequest(rs.getInt("id"), rs.getInt("student_id"),
                        rs.getInt("book_id"), LocalDate.parse(rs.getString("start_date")),
                        LocalDate.parse(rs.getString("end_date")),
                        LocalDateTime.parse(rs.getString("request_time")));
                request.setStatus(RequestStatus.valueOf(rs.getString("status")));
                String reviewer = rs.getString("reviewer_username");
                String reviewTime = rs.getString("review_time");
                if (reviewer != null) {
                    request.setReview(reviewer, reviewTime == null ? null : LocalDateTime.parse(reviewTime));
                }
                requests.add(request);
            }
        }

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM loans ORDER BY id")) {
            while (rs.next()) {
                Loan loan = new Loan(rs.getInt("id"), rs.getInt("request_id"), rs.getInt("student_id"),
                        rs.getInt("book_id"), rs.getString("loan_employee_username"),
                        LocalDateTime.parse(rs.getString("borrow_time")),
                        LocalDate.parse(rs.getString("due_date")));
                String returnTime = rs.getString("return_time");
                String returnEmployee = rs.getString("return_employee_username");
                if (returnTime != null) {
                    loan.close(LocalDateTime.parse(returnTime), returnEmployee);
                }
                loans.add(loan);
            }
        }
    }

    public int nextUserId() {
        return users.stream().mapToInt(User::getId).max().orElse(0) + 1;
    }

    public int nextBookId() {
        return books.stream().mapToInt(Book::getId).max().orElse(0) + 1;
    }

    public int nextRequestId() {
        return requests.stream().mapToInt(LoanRequest::getId).max().orElse(0) + 1;
    }

    public int nextLoanId() {
        return loans.stream().mapToInt(Loan::getId).max().orElse(0) + 1;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<LoanRequest> getRequests() {
        return requests;
    }

    public List<Loan> getLoans() {
        return loans;
    }
}
