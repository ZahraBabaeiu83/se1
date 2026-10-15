package Library;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LibraryService {

    private final Database db = Database.getInstance();

    public User login(String username, String password) throws LibraryException {
        User user = findUser(username);
        if (user == null || !user.checkPassword(password)) {
            throw new LibraryException("نام کاربری یا رمز عبور نادرست است.");
        }
        return user;
    }

    public Student registerStudent(String username, String password, String confirm, String fullName)
            throws LibraryException {
        ensureFreeUsername(username);
        ensurePassword(password, confirm);
        Student student = new Student(db.nextUserId(), username.trim(), password, fullName);
        db.getUsers().add(student);
        db.save();
        return student;
    }

    public Employee createEmployee(String username, String password, String confirm, String fullName)
            throws LibraryException {
        ensureFreeUsername(username);
        ensurePassword(password, confirm);
        Employee employee = new Employee(db.nextUserId(), username.trim(), password, fullName);
        db.getUsers().add(employee);
        db.save();
        return employee;
    }

    public void changePassword(User user, String current, String newPassword, String confirm)
            throws LibraryException {
        if (!user.checkPassword(current)) {
            throw new LibraryException("رمز عبور فعلی نادرست است.");
        }
        ensurePassword(newPassword, confirm);
        user.setPassword(newPassword);
        db.save();
    }

    private void ensurePassword(String password, String confirm) throws LibraryException {
        if (password == null || password.trim().length() < 4) {
            throw new LibraryException("رمز عبور باید حداقل ۴ نویسه باشد.");
        }
        if (!password.equals(confirm)) {
            throw new LibraryException("رمز عبور و تکرار آن یکسان نیستند.");
        }
    }

    private void ensureFreeUsername(String username) throws LibraryException {
        if (username == null || username.trim().isEmpty()) {
            throw new LibraryException("نام کاربری نمی‌تواند خالی باشد.");
        }
        if (findUser(username.trim()) != null) {
            throw new LibraryException("این نام کاربری قبلا استفاده شده است.");
        }
    }

    public User findUser(String username) {
        for (User user : db.getUsers()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public User findUserById(int id) {
        for (User user : db.getUsers()) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    public Student findStudent(String username) {
        User user = findUser(username);
        return user instanceof Student ? (Student) user : null;
    }

    public List<Student> getStudents() {
        List<Student> students = new ArrayList<>();
        for (User user : db.getUsers()) {
            if (user instanceof Student) {
                students.add((Student) user);
            }
        }
        return students;
    }

    public List<Employee> getEmployees() {
        List<Employee> employees = new ArrayList<>();
        for (User user : db.getUsers()) {
            if (user instanceof Employee) {
                employees.add((Employee) user);
            }
        }
        return employees;
    }

    private Student getStudent(int id) {
        User user = findUserById(id);
        return user instanceof Student ? (Student) user : null;
    }

    public void setStudentActive(Student student, boolean active) {
        student.setActive(active);
        db.save();
    }

    public Book addBook(String title, String author, int publishYear, Employee by)
            throws LibraryException {
        validateBook(title, author, publishYear);
        Book book = new Book(db.nextBookId(), title.trim(), author.trim(), publishYear,
                by.getUsername());
        db.getBooks().add(book);
        db.save();
        return book;
    }

    private void validateBook(String title, String author, int publishYear) throws LibraryException {
        if (title == null || title.trim().isEmpty()) {
            throw new LibraryException("عنوان کتاب نمی‌تواند خالی باشد.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new LibraryException("نام نویسنده نمی‌تواند خالی باشد.");
        }
        int currentYear = LocalDate.now().getYear();
        if (publishYear < 1000 || publishYear > currentYear + 1) {
            throw new LibraryException("سال نشر معتبر نیست.");
        }
    }

    /**
     * ویرایش اطلاعات کتاب (3-4). مقادیر null/خالی یعنی تغییر نکردن.
     */
    public void updateBook(Book book, String title, String author, Integer publishYear)
            throws LibraryException {
        String newTitle = (title == null || title.isBlank()) ? book.getTitle() : title;
        String newAuthor = (author == null || author.isBlank()) ? book.getAuthor() : author;
        int newYear = publishYear == null ? book.getPublishYear() : publishYear;
        validateBook(newTitle, newAuthor, newYear);
        book.setTitle(newTitle.trim());
        book.setAuthor(newAuthor.trim());
        book.setPublishYear(newYear);
        db.save();
    }

    public List<Book> searchBooks(String titlePart, String authorPart, Integer year) {
        List<Book> result = new ArrayList<>();
        for (Book book : db.getBooks()) {
            if (book.matches(titlePart, authorPart, year)) {
                result.add(book);
            }
        }
        return result;
    }

    public List<Book> searchBooksByTitle(String titlePart) {
        List<Book> result = new ArrayList<>();
        for (Book book : db.getBooks()) {
            if (book.matchesTitle(titlePart)) {
                result.add(book);
            }
        }
        return result;
    }

    public Book findBook(int id) {
        for (Book book : db.getBooks()) {
            if (book.getId() == id) {
                return book;
            }
        }
        return null;
    }

    public boolean isBookAvailable(int bookId) {
        for (Loan loan : db.getLoans()) {
            if (loan.getBookId() == bookId && loan.isActive()) {
                return false;
            }
        }
        return true;
    }

    public LoanRequest createRequest(Student student, int bookId, LocalDate start, LocalDate end)
            throws LibraryException {
        if (!student.isActive()) {
            throw new LibraryException("حساب شما غیرفعال است و امکان ثبت درخواست امانت ندارید.");
        }
        if (findBook(bookId) == null) {
            throw new LibraryException("کتابی با این کد یافت نشد.");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new LibraryException("زمان شروع نمی‌تواند قبل از امروز باشد.");
        }
        if (!end.isAfter(start)) {
            throw new LibraryException("زمان پایان باید بعد از زمان شروع باشد.");
        }
        for (LoanRequest r : db.getRequests()) {
            if (r.getStudentId() == student.getId() && r.getBookId() == bookId
                    && (r.getStatus() == RequestStatus.PENDING
                    || r.getStatus() == RequestStatus.APPROVED)) {
                throw new LibraryException("برای این کتاب درخواست جاری (در انتظار بررسی یا تاییدشده) دارید.");
            }
        }
        LoanRequest request = new LoanRequest(db.nextRequestId(), student.getId(), bookId,
                start, end, LocalDateTime.now());
        db.getRequests().add(request);
        db.save();
        return request;
    }

    public LoanRequest findRequest(int id) {
        for (LoanRequest request : db.getRequests()) {
            if (request.getId() == id) {
                return request;
            }
        }
        return null;
    }

    private LoanRequest mustFindRequest(int id) throws LibraryException {
        LoanRequest request = findRequest(id);
        if (request == null) {
            throw new LibraryException("درخواستی با این کد یافت نشد.");
        }
        return request;
    }

    public List<LoanRequest> getRequestsForApproval() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        List<LoanRequest> result = new ArrayList<>();
        for (LoanRequest r : db.getRequests()) {
            if (r.getStatus() == RequestStatus.PENDING
                    && (r.getStartDate().equals(today) || r.getStartDate().equals(yesterday))) {
                result.add(r);
            }
        }
        return result;
    }

    public void approveRequest(Employee employee, int requestId) throws LibraryException {
        LoanRequest request = mustFindRequest(requestId);
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new LibraryException("این درخواست قبلا بررسی شده است.");
        }
        Student student = getStudent(request.getStudentId());
        if (student == null) {
            throw new LibraryException("دانشجوی درخواست‌کننده یافت نشد.");
        }
        if (!student.isActive()) {
            throw new LibraryException("دانشجو غیرفعال است؛ درخواست قابل تایید نیست.");
        }
        if (!isBookAvailable(request.getBookId())) {
            throw new LibraryException("کتاب هم‌اکنون در امانت است؛ درخواست قابل تایید نیست.");
        }
        request.setStatus(RequestStatus.APPROVED);
        request.setReview(employee.getUsername(), LocalDateTime.now());
        db.save();
    }

    public void rejectRequest(Employee employee, int requestId) throws LibraryException {
        LoanRequest request = mustFindRequest(requestId);
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new LibraryException("این درخواست قبلا بررسی شده است.");
        }
        request.setStatus(RequestStatus.REJECTED);
        request.setReview(employee.getUsername(), LocalDateTime.now());
        db.save();
    }

    public List<LoanRequest> getApprovedRequests() {
        List<LoanRequest> result = new ArrayList<>();
        for (LoanRequest r : db.getRequests()) {
            if (r.getStatus() == RequestStatus.APPROVED) {
                result.add(r);
            }
        }
        return result;
    }

    public List<LoanRequest> requestsOfStudent(int studentId) {
        List<LoanRequest> result = new ArrayList<>();
        for (LoanRequest r : db.getRequests()) {
            if (r.getStudentId() == studentId) {
                result.add(r);
            }
        }
        return result;
    }

    public Loan handOverBook(Employee employee, int requestId) throws LibraryException {
        LoanRequest request = mustFindRequest(requestId);
        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new LibraryException("تنها درخواست‌های تاییدشده قابل تحویل هستند.");
        }
        Student student = getStudent(request.getStudentId());
        if (student == null) {
            throw new LibraryException("دانشجوی درخواست‌کننده یافت نشد.");
        }
        if (!student.isActive()) {
            throw new LibraryException("دانشجو غیرفعال است؛ کتاب قابل تحویل نیست.");
        }
        if (!isBookAvailable(request.getBookId())) {
            throw new LibraryException("کتاب هم‌اکنون در امانت است.");
        }
        Loan loan = new Loan(db.nextLoanId(), request.getId(), request.getStudentId(),
                request.getBookId(), employee.getUsername(), LocalDateTime.now(),
                request.getEndDate());
        db.getLoans().add(loan);
        request.setStatus(RequestStatus.BORROWED);
        db.save();
        return loan;
    }

    public Loan findLoan(int id) {
        for (Loan loan : db.getLoans()) {
            if (loan.getId() == id) {
                return loan;
            }
        }
        return null;
    }

    public Loan returnBook(Employee employee, int loanId) throws LibraryException {
        Loan loan = findLoan(loanId);
        if (loan == null) {
            throw new LibraryException("امانتی با این کد یافت نشد.");
        }
        if (!loan.isActive()) {
            throw new LibraryException("این امانت پیش‌تر بسته شده است.");
        }
        loan.close(LocalDateTime.now(), employee.getUsername());
        db.save();
        return loan;
    }

    public List<Loan> getActiveLoans() {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : db.getLoans()) {
            if (loan.isActive()) {
                result.add(loan);
            }
        }
        return result;
    }

    public List<Loan> loansOfStudent(int studentId) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : db.getLoans()) {
            if (loan.getStudentId() == studentId) {
                result.add(loan);
            }
        }
        return result;
    }

    public List<Loan> activeLoansOfStudent(int studentId) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : db.getLoans()) {
            if (loan.getStudentId() == studentId && loan.isActive()) {
                result.add(loan);
            }
        }
        return result;
    }

    public long countStudents() {
        return getStudents().size();
    }

    public long countBooks() {
        return db.getBooks().size();
    }

    public long countLoans() {
        return db.getLoans().size();
    }

    public long countActiveLoans() {
        return getActiveLoans().size();
    }

    public StudentStats statsOfStudent(int studentId) {
        int total = 0;
        int unreturned = 0;
        int lateReturned = 0;
        long totalLateDays = 0;
        for (Loan loan : db.getLoans()) {
            if (loan.getStudentId() != studentId) {
                continue;
            }
            total++;
            if (loan.isActive()) {
                unreturned++;
            } else {
                long late = loan.lateDays();
                if (late > 0) {
                    lateReturned++;
                    totalLateDays += late;
                }
            }
        }
        return new StudentStats(total, unreturned, lateReturned, totalLateDays);
    }

    public EmployeeStats statsOfEmployee(String username) {
        int registered = 0;
        int given = 0;
        int received = 0;
        for (Book book : db.getBooks()) {
            if (book.getRegisteredBy().equals(username)) {
                registered++;
            }
        }
        for (Loan loan : db.getLoans()) {
            if (username.equals(loan.getLoanEmployeeUsername())) {
                given++;
            }
            if (loan.getReturnEmployeeUsername() != null
                    && username.equals(loan.getReturnEmployeeUsername())) {
                received++;
            }
        }
        return new EmployeeStats(registered, given, received);
    }

    public LoanStats loanStats() {
        int totalRequests = db.getRequests().size();
        int totalLoans = db.getLoans().size();
        long sumDays = 0;
        int returnedCount = 0;
        for (Loan loan : db.getLoans()) {
            long days = loan.durationDays();
            if (days >= 0) {
                sumDays += days;
                returnedCount++;
            }
        }
        double average = returnedCount == 0 ? 0.0 : (double) sumDays / returnedCount;
        return new LoanStats(totalRequests, totalLoans, returnedCount, average);
    }

    public List<Student> topLateStudents(int limit) {
        Map<Integer, Long> lateDaysByStudent = new HashMap<>();
        for (Loan loan : db.getLoans()) {
            if (loan.isActive()) {
                continue;
            }
            long late = loan.lateDays();
            if (late > 0) {
                lateDaysByStudent.merge(loan.getStudentId(), late, Long::sum);
            }
        }
        List<Student> candidates = new ArrayList<>();
        for (Student student : getStudents()) {
            if (lateDaysByStudent.containsKey(student.getId())) {
                candidates.add(student);
            }
        }
        candidates.sort((a, b) -> Long.compare(
                lateDaysByStudent.get(b.getId()), lateDaysByStudent.get(a.getId())));
        return candidates.size() > limit
                ? new ArrayList<>(candidates.subList(0, limit))
                : candidates;
    }

    public String displayUser(int userId) {
        User user = findUserById(userId);
        return user == null ? ("کاربر#" + userId) : user.getUsername();
    }

    public String displayBook(int bookId) {
        Book book = findBook(bookId);
        return book == null ? ("کتاب#" + bookId) : book.getTitle();
    }
}
