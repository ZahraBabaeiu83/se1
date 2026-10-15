package Library;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class Library {

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, "UTF-8"));
        } catch (Exception ignored) {

        }

        Database.getInstance().load();
        new ConsoleUI().run();
    }
}

enum RequestStatus {

    PENDING("در انتظار بررسی"),

    APPROVED("تاییدشده — در انتظار مراجعه دانشجو"),

    REJECTED("ردشده"),

    BORROWED("امانت‌داده‌شده");

    private final String fa;

    RequestStatus(String fa) {
        this.fa = fa;
    }

    public String getFa() {
        return fa;
    }
}

class StudentStats {

    public final int totalLoans;
    public final int unreturned;
    public final int lateReturned;
    public final long totalLateDays;

    public StudentStats(int totalLoans, int unreturned, int lateReturned, long totalLateDays) {
        this.totalLoans = totalLoans;
        this.unreturned = unreturned;
        this.lateReturned = lateReturned;
        this.totalLateDays = totalLateDays;
    }
}

class EmployeeStats {

    public final int booksRegistered;
    public final int loansGiven;
    public final int loansReceived;

    public EmployeeStats(int booksRegistered, int loansGiven, int loansReceived) {
        this.booksRegistered = booksRegistered;
        this.loansGiven = loansGiven;
        this.loansReceived = loansReceived;
    }
}

class LoanStats {

    public final int totalRequests;
    public final int totalLoans;
    public final int returnedLoans;
    public final double averageDays;

    public LoanStats(int totalRequests, int totalLoans, int returnedLoans, double averageDays) {
        this.totalRequests = totalRequests;
        this.totalLoans = totalLoans;
        this.returnedLoans = returnedLoans;
        this.averageDays = averageDays;
    }
}

class LibraryException extends Exception {

    public LibraryException(String message) {
        super(message);
    }
}

class ConsoleUI {

    private final LibraryService service = new LibraryService();
    private final StudentMenu studentMenu = new StudentMenu(service);
    private final EmployeeMenu employeeMenu = new EmployeeMenu(service);
    private final ManagerMenu managerMenu = new ManagerMenu(service);

    public void run() {
        if (Database.getInstance().isFirstRun()) {
            System.out.println("*** نخستین اجرای برنامه: مدیر سیستم با مشخصات پیش‌فرض ساخته شد ***");
            System.out.println("    نام کاربری: admin   |   رمز عبور: admin123");
            System.out.println("    (پس از ورود، رمز عبور مدیر را تغییر دهید.)");
        }
        while (true) {
            System.out.println();
            System.out.println("========== سامانه مدیریت کتابخانه دانشگاه ==========");
            System.out.println("1) ورود به سیستم");
            System.out.println("2) ثبت‌نام دانشجو");
            System.out.println("3) ورود به عنوان کاربر مهمان");
            System.out.println("0) خروج از برنامه");
            int choice = InputUtil.readInt("گزینه: ");
            try {
                switch (choice) {
                    case 1:
                        doLogin();
                        break;
                    case 2:
                        doRegisterStudent();
                        break;
                    case 3:
                        guestMenu();
                        break;
                    case 0:
                        System.out.println("با تشکر از استفاده. خدانگهدار!");
                        return;
                    default:
                        System.out.println("گزینه نامعتبر است.");
                }
            } catch (LibraryException e) {
                System.out.println("خطا: " + e.getMessage());
            }
        }
    }

    private void doLogin() throws LibraryException {
        String username = InputUtil.readNonEmpty("نام کاربری: ");
        String password = InputUtil.readNonEmpty("رمز عبور: ");
        User user = service.login(username, password);
        System.out.println("خوش آمدید — " + user.getUsername() + " (" + user.getRoleName() + ")");
        if (user instanceof Student) {
            studentMenu.run((Student) user);
        } else if (user instanceof Employee) {
            employeeMenu.run((Employee) user);
        } else if (user instanceof Manager) {
            managerMenu.run((Manager) user);
        }
    }

    private void doRegisterStudent() throws LibraryException {
        String username = InputUtil.readNonEmpty("نام کاربری: ");
        String password = InputUtil.readNonEmpty("رمز عبور (حداقل ۴ نویسه): ");
        String confirm = InputUtil.readNonEmpty("تکرار رمز عبور: ");
        String fullName = InputUtil.readLine("نام و نام خانوادگی (اختیاری): ");
        Student student = service.registerStudent(username, password, confirm, fullName);
        System.out.println("ثبت‌نام انجام شد؛ اکنون با نام کاربری «"
                + student.getUsername() + "» وارد شوید.");
    }

    private void guestMenu() {
        while (true) {
            System.out.println();
            System.out.println("===== منوی کاربر مهمان =====");
            System.out.println("1) مشاهده تعداد دانشجویان ثبت‌نام‌کرده");
            System.out.println("2) جستجوی کتاب بر اساس نام کتاب");
            System.out.println("3) مشاهده اطلاعات آماری");
            System.out.println("0) بازگشت");
            int choice = InputUtil.readInt("گزینه: ");
            switch (choice) {
                case 1:
                    System.out.println("تعداد دانشجویان ثبت‌نام‌کرده: " + service.countStudents());
                    break;
                case 2:
                    String title = InputUtil.readLine("بخشی از نام کتاب: ");
                    List<Book> results = service.searchBooksByTitle(title);
                    if (results.isEmpty()) {
                        System.out.println("کتابی یافت نشد.");
                    } else {
                        for (Book book : results) {
                            System.out.println(book.toDisplayString());
                        }
                        System.out.println("تعداد نتایج: " + results.size());
                    }
                    break;
                case 3:
                    System.out.println("--- اطلاعات آماری کتابخانه ---");
                    System.out.println("تعداد کل دانشجویان: " + service.countStudents());
                    System.out.println("تعداد کل کتاب‌ها: " + service.countBooks());
                    System.out.println("تعداد کل امانت‌ها: " + service.countLoans());
                    System.out.println("تعداد کتاب‌هایی که هم‌اکنون در امانت هستند: "
                            + service.countActiveLoans());
                    break;
                case 0:
                    return;
                default:
                    System.out.println("گزینه نامعتبر است.");
            }
        }
    }
}

class ManagerMenu {

    private final LibraryService service;

    public ManagerMenu(LibraryService service) {
        this.service = service;
    }

    public void run(Manager manager) {
        while (true) {
            System.out.println();
            System.out.println("===== منوی مدیر سیستم (" + manager.getUsername() + ") =====");
            System.out.println("1) تعریف کارمند کتابخانه");
            System.out.println("2) مشاهده عملکرد کارمندان");
            System.out.println("3) مشاهده اطلاعات آماری امانات کتاب");
            System.out.println("4) مشاهده اطلاعات آماری دانشجویان");
            System.out.println("5) تغییر رمز عبور");
            System.out.println("0) خروج از حساب");
            int choice = InputUtil.readInt("گزینه: ");
            try {
                switch (choice) {
                    case 1:
                        createEmployee();
                        break;
                    case 2:
                        employeePerformance();
                        break;
                    case 3:
                        loanStatistics();
                        break;
                    case 4:
                        studentStatistics();
                        break;
                    case 5:
                        CommonFlows.changePassword(service, manager);
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("گزینه نامعتبر است.");
                }
            } catch (LibraryException e) {
                System.out.println("خطا: " + e.getMessage());
            }
        }
    }

    private void createEmployee() throws LibraryException {
        String username = InputUtil.readNonEmpty("نام کاربری کارمند: ");
        String password = InputUtil.readNonEmpty("رمز عبور (حداقل ۴ نویسه): ");
        String confirm = InputUtil.readNonEmpty("تکرار رمز عبور: ");
        String fullName = InputUtil.readLine("نام و نام خانوادگی (اختیاری): ");
        Employee employee = service.createEmployee(username, password, confirm, fullName);
        System.out.println("کارمند «" + employee.getUsername() + "» با موفقیت تعریف شد.");
    }

    private void employeePerformance() {
        List<Employee> employees = service.getEmployees();
        if (employees.isEmpty()) {
            System.out.println("هنوز کارمندی تعریف نشده است.");
            return;
        }
        System.out.println("--- عملکرد کارمندان کتابخانه ---");
        for (Employee employee : employees) {
            EmployeeStats stats = service.statsOfEmployee(employee.getUsername());
            String name = employee.getFullName().isEmpty()
                    ? "" : " (" + employee.getFullName() + ")";
            System.out.println("کارمند: " + employee.getUsername() + name);
            System.out.println("    تعداد کتاب‌هایی که ثبت کرده: " + stats.booksRegistered);
            System.out.println("    تعداد کل کتاب‌هایی که امانت داده: " + stats.loansGiven);
            System.out.println("    تعداد کل کتاب‌هایی که تحویل گرفته: " + stats.loansReceived);
        }
    }

    private void loanStatistics() {
        LoanStats stats = service.loanStats();
        System.out.println("--- اطلاعات آماری امانات کتاب ---");
        System.out.println("تعداد درخواست‌های امانت ثبت‌شده: " + stats.totalRequests);
        System.out.println("تعداد کل امانت‌های داده‌شده: " + stats.totalLoans);
        System.out.println("تعداد امانت‌های برگردانده‌شده: " + stats.returnedLoans);
        System.out.printf(Locale.US,
                "میانگین تعداد روزهای امانت (از دریافت تا برگرداندن): %.1f روز%n",
                stats.averageDays);
    }

    private void studentStatistics() {
        List<Student> students = service.getStudents();
        if (students.isEmpty()) {
            System.out.println("هنوز دانشجویی ثبت‌نام نکرده است.");
            return;
        }
        System.out.println("--- اطلاعات آماری امانات دانشجویان ---");
        for (Student student : students) {
            StudentStats stats = service.statsOfStudent(student.getId());
            System.out.println("دانشجو: " + student.getUsername()
                    + " (" + student.statusFa() + ")"
                    + " | تعداد کل امانات: " + stats.totalLoans
                    + " | تحویل‌داده‌نشده: " + stats.unreturned
                    + " | تحویل‌داده‌شده با تاخیر: " + stats.lateReturned
                    + " | مجموع روزهای تاخیر: " + stats.totalLateDays);
        }
        List<Student> top = service.topLateStudents(10);
        if (top.isEmpty()) {
            System.out.println("دانشجویی با تاخیر در تحویل کتاب وجود ندارد.");
            return;
        }
        System.out.println("--- ۱۰ دانشجوی با بیشترین تاخیر در تحویل کتاب ---");
        int rank = 1;
        for (Student student : top) {
            StudentStats stats = service.statsOfStudent(student.getId());
            System.out.println(rank + ") " + student.getUsername()
                    + " | مجموع روزهای تاخیر: " + stats.totalLateDays
                    + " | تعداد امانت‌های تاخیری: " + stats.lateReturned);
            rank++;
        }
    }
}

class EmployeeMenu {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LibraryService service;
    private Employee employee;

    public EmployeeMenu(LibraryService service) {
        this.service = service;
    }

    public void run(Employee employee) {
        this.employee = employee;
        while (true) {
            System.out.println();
            System.out.println("===== منوی کارمند کتابخانه (" + employee.getUsername() + ") =====");
            System.out.println("1) تغییر رمز عبور");
            System.out.println("2) ثبت اطلاعات کتاب");
            System.out.println("3) جستجو و ویرایش اطلاعات کتاب");
            System.out.println("4) بررسی و تایید درخواست‌های امانت");
            System.out.println("5) تحویل کتاب به دانشجو (ثبت امانت)");
            System.out.println("6) دریافت کتاب برگردانده‌شده");
            System.out.println("7) گزارش امانات یک دانشجو");
            System.out.println("8) فعال/غیرفعال کردن دانشجو");
            System.out.println("0) خروج از حساب");
            int choice = InputUtil.readInt("گزینه: ");
            try {
                switch (choice) {
                    case 1:
                        CommonFlows.changePassword(service, employee);
                        break;

                    case 2:
                        addBook();
                        break;

                    case 3:
                        searchAndEditBooks();
                        break;

                    case 4:
                        reviewRequests();
                        break;

                    case 5:
                        handOverBook();
                        break;

                    case 6:
                        receiveReturnedBook();
                        break;

                    case 7:
                        studentReport();
                        break;

                    case 8:
                        toggleStudent();
                        break;

                    case 0:
                        return;
                    default:
                        System.out.println("گزینه نامعتبر است.");
                }
            } catch (LibraryException e) {
                System.out.println("خطا: " + e.getMessage());
            }
        }
    }

    private void addBook() throws LibraryException {
        String title = InputUtil.readNonEmpty("عنوان کتاب: ");
        String author = InputUtil.readNonEmpty("نام نویسنده: ");
        int year = InputUtil.readInt("سال نشر: ");
        Book book = service.addBook(title, author, year, employee);
        System.out.println("کتاب با موفقیت ثبت شد. کد کتاب: " + book.getId());
    }

    private void searchAndEditBooks() throws LibraryException {
        List<Book> results = CommonFlows.searchBooksFlow(service, true);
        if (results.isEmpty()) {
            return;
        }
        int id = InputUtil.readInt("کد کتاب برای ویرایش (0 = بازگشت): ");
        if (id == 0) {
            return;
        }
        Book book = service.findBook(id);
        if (book == null) {
            System.out.println("کتابی با این کد یافت نشد.");
            return;
        }
        System.out.println("برای نگه‌داشتن هر مقدار فعلی، فقط Enter بزنید.");
        String title = InputUtil.readLine("عنوان جدید [" + book.getTitle() + "]: ");
        String author = InputUtil.readLine("نویسنده جدید [" + book.getAuthor() + "]: ");
        Integer year = InputUtil.readOptionalInt("سال نشر جدید [" + book.getPublishYear() + "]: ");
        service.updateBook(book, title, author, year);
        System.out.println("اطلاعات کتاب با موفقیت به‌روزرسانی شد.");
    }

    private void reviewRequests() throws LibraryException {
        while (true) {
            List<LoanRequest> pending = service.getRequestsForApproval();
            if (pending.isEmpty()) {
                System.out.println("درخواستی برای بررسی (با تاریخ شروع امروز یا دیروز) وجود ندارد.");
                return;
            }
            System.out.println("--- درخواست‌های در انتظار بررسی ---");
            for (LoanRequest request : pending) {
                printRequest(request);
            }
            int id = InputUtil.readInt("کد درخواست برای بررسی (0 = بازگشت): ");
            if (id == 0) {
                return;
            }
            LoanRequest request = service.findRequest(id);
            if (request == null) {
                System.out.println("درخواستی با این کد یافت نشد.");
                continue;
            }
            printRequest(request);
            int action = InputUtil.readInt("1) تایید   2) رد   3) انصراف — گزینه: ");
            if (action == 1) {
                service.approveRequest(employee, id);
                System.out.println("درخواست «" + id + "» تایید شد؛ دانشجو می‌تواند برای دریافت کتاب مراجعه کند.");
            } else if (action == 2) {
                service.rejectRequest(employee, id);
                System.out.println("درخواست «" + id + "» رد شد.");
            } else if (action != 3) {
                System.out.println("گزینه نامعتبر است.");
            }
        }
    }

    private void handOverBook() throws LibraryException {
        List<LoanRequest> approved = service.getApprovedRequests();
        if (approved.isEmpty()) {
            System.out.println("درخواست تاییدشده‌ای در انتظار تحویل کتاب وجود ندارد.");
            return;
        }
        System.out.println("--- درخواست‌های تاییدشده در انتظار مراجعه دانشجو ---");
        for (LoanRequest request : approved) {
            printRequest(request);
        }
        int id = InputUtil.readInt("کد درخواستی که کتاب آن تحویل دانشجو شد (0 = بازگشت): ");
        if (id == 0) {
            return;
        }
        Loan loan = service.handOverBook(employee, id);
        System.out.println("امانت با کد " + loan.getId() + " ثبت شد. موعد برگرداندن: "
                + loan.getDueDate());
    }

    private void receiveReturnedBook() throws LibraryException {
        List<Loan> activeLoans = service.getActiveLoans();
        if (activeLoans.isEmpty()) {
            System.out.println("در حال حاضر امانت بازی وجود ندارد.");
            return;
        }
        System.out.println("--- امانت‌های باز (کتاب‌های تحویل‌نگرفته) ---");
        for (Loan loan : activeLoans) {
            printLoan(loan);
        }
        int id = InputUtil.readInt("کد امانتی که کتاب آن برگردانده شد (0 = بازگشت): ");
        if (id == 0) {
            return;
        }
        Loan loan = service.returnBook(employee, id);
        long late = loan.lateDays();
        if (late > 0) {
            System.out.println("کتاب با " + late + " روز تاخیر دریافت و امانت بسته شد.");
        } else {
            System.out.println("کتاب بدون تاخیر دریافت و امانت بسته شد.");
        }
    }

    private void studentReport() throws LibraryException {
        String username = InputUtil.readNonEmpty("نام کاربری دانشجو: ");
        Student student = service.findStudent(username);
        if (student == null) {
            System.out.println("دانشجویی با این نام کاربری یافت نشد.");
            return;
        }
        List<Loan> loans = service.loansOfStudent(student.getId());
        System.out.println("--- تاریخچه امانات دانشجو «" + username + "» ("
                + loans.size() + " رکورد) ---");
        for (Loan loan : loans) {
            printLoan(loan);
        }
        StudentStats stats = service.statsOfStudent(student.getId());
        System.out.println("تعداد کل امانات: " + stats.totalLoans);
        System.out.println("تعداد کل کتاب‌های تحویل‌داده‌نشده: " + stats.unreturned);
        System.out.println("تعداد کل امانت‌های تحویل‌داده‌شده با تاخیر: " + stats.lateReturned);
        System.out.println("مجموع روزهای تاخیر: " + stats.totalLateDays);
    }

    private void toggleStudent() throws LibraryException {
        String username = InputUtil.readNonEmpty("نام کاربری دانشجو: ");
        Student student = service.findStudent(username);
        if (student == null) {
            System.out.println("دانشجویی با این نام کاربری یافت نشد.");
            return;
        }
        System.out.println("وضعیت فعلی دانشجو: " + student.statusFa());
        boolean active = InputUtil.readYesNo("این دانشجو فعال باشد؟ (بله/خیر): ");
        service.setStudentActive(student, active);
        System.out.println("وضعیت دانشجو به «" + student.statusFa() + "» تغییر کرد.");
    }

    private void printRequest(LoanRequest request) {
        System.out.println("کد درخواست: " + request.getId()
                + " | دانشجو: " + service.displayUser(request.getStudentId())
                + " | کتاب: " + service.displayBook(request.getBookId())
                + " | از " + request.getStartDate() + " تا " + request.getEndDate()
                + " | وضعیت: " + request.getStatus().getFa()
                + " | زمان ثبت: " + formatDateTime(request.getRequestTime()));
    }

    private void printLoan(Loan loan) {
        StringBuilder line = new StringBuilder();
        line.append("کد امانت: ").append(loan.getId())
                .append(" | دانشجو: ").append(service.displayUser(loan.getStudentId()))
                .append(" | کتاب: ").append(service.displayBook(loan.getBookId()))
                .append(" | زمان دریافت کتاب: ").append(formatDateTime(loan.getBorrowTime()))
                .append(" | موعد برگشت: ").append(loan.getDueDate());
        if (loan.isActive()) {
            line.append(" | وضعیت: باز (برنگشته)");
            long overdue = loan.overdueDaysUntilToday();
            if (overdue > 0) {
                line.append(" — ").append(overdue).append(" روز از موعد گذشته است");
            }
        } else {
            line.append(" | زمان برگشت: ").append(formatDateTime(loan.getReturnTime()));
            long late = loan.lateDays();
            if (late > 0) {
                line.append(" (").append(late).append(" روز تاخیر)");
            } else {
                line.append(" (بدون تاخیر)");
            }
        }
        System.out.println(line);
    }

    private static String formatDateTime(LocalDateTime time) {
        return time == null ? "-" : DATE_TIME_FORMAT.format(time);
    }
}
