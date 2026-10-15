package Library;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

class StudentMenu {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LibraryService service;

    public StudentMenu(LibraryService service) {
        this.service = service;
    }

    public void run(Student student) {
        while (true) {
            System.out.println();
            System.out.println("===== منوی دانشجو (" + student.getUsername() + ") =====");
            System.out.println("1) جستجوی کتاب");
            System.out.println("2) ثبت درخواست امانت کتاب");
            System.out.println("3) درخواست‌ها و امانت‌های من");
            System.out.println("4) تغییر رمز عبور");
            System.out.println("0) خروج از حساب");
            int choice = InputUtil.readInt("گزینه: ");
            try {
                switch (choice) {
                    case 1:
                        CommonFlows.searchBooksFlow(service, true);
                        break;

                    case 2:
                        createRequest(student);
                        break;

                    case 3:
                        myRequests(student);
                        break;

                    case 4:
                        CommonFlows.changePassword(service, student);
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

    private void createRequest(Student student) throws LibraryException {
        System.out.println("ابتدا کتاب مورد نظر خود را جستجو کنید:");
        CommonFlows.searchBooksFlow(service, true);
        int bookId = InputUtil.readInt("کد کتاب درخواستی (0 = انصراف): ");
        if (bookId == 0) {
            return;
        }
        LocalDate start = InputUtil.readDate("تاریخ شروع امانت (yyyy-mm-dd): ");
        LocalDate end = InputUtil.readDate("تاریخ پایان امانت (yyyy-mm-dd): ");
        LoanRequest request = service.createRequest(student, bookId, start, end);
        System.out.println("درخواست امانت با کد " + request.getId()
                + " ثبت شد و در انتظار بررسی کارمند کتابخانه است.");
    }

    private void myRequests(Student student) {
        List<LoanRequest> requests = service.requestsOfStudent(student.getId());
        if (requests.isEmpty()) {
            System.out.println("هنوز درخواستی ثبت نکرده‌اید.");
        } else {
            System.out.println("--- درخواست‌های من ---");
            for (LoanRequest request : requests) {
                System.out.println("کد درخواست: " + request.getId()
                        + " | کتاب: " + service.displayBook(request.getBookId())
                        + " | از " + request.getStartDate() + " تا " + request.getEndDate()
                        + " | وضعیت: " + request.getStatus().getFa()
                        + " | ثبت: " + DATE_TIME_FORMAT.format(request.getRequestTime()));
            }
        }
        List<Loan> activeLoans = service.activeLoansOfStudent(student.getId());
        if (!activeLoans.isEmpty()) {
            System.out.println("--- امانت‌های جاری من ---");
            for (Loan loan : activeLoans) {
                System.out.println("کد امانت: " + loan.getId()
                        + " | کتاب: " + service.displayBook(loan.getBookId())
                        + " | تاریخ دریافت: " + DATE_TIME_FORMAT.format(loan.getBorrowTime())
                        + " | موعد برگشت: " + loan.getDueDate());
            }
        }
    }
}
