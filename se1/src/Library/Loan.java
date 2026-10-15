package Library;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

class Loan implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final int requestId;
    private final int studentId;
    private final int bookId;
    private final String loanEmployeeUsername;
    private final LocalDateTime borrowTime;
    private final LocalDate dueDate;

    private LocalDateTime returnTime;
    private String returnEmployeeUsername;

    public Loan(int id, int requestId, int studentId, int bookId,
                String loanEmployeeUsername, LocalDateTime borrowTime, LocalDate dueDate) {
        this.id = id;
        this.requestId = requestId;
        this.studentId = studentId;
        this.bookId = bookId;
        this.loanEmployeeUsername = loanEmployeeUsername;
        this.borrowTime = borrowTime;
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getBookId() {
        return bookId;
    }

    public String getLoanEmployeeUsername() {
        return loanEmployeeUsername;
    }

    public LocalDateTime getBorrowTime() {
        return borrowTime;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getReturnTime() {
        return returnTime;
    }

    public String getReturnEmployeeUsername() {
        return returnEmployeeUsername;
    }

    public boolean isActive() {
        return returnTime == null;
    }

    public void close(LocalDateTime returnTime, String returnEmployeeUsername) {
        this.returnTime = returnTime;
        this.returnEmployeeUsername = returnEmployeeUsername;
    }

    public long lateDays() {
        if (returnTime == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(dueDate, returnTime.toLocalDate());
        return Math.max(0, days);
    }

    public long durationDays() {
        if (returnTime == null) {
            return -1;
        }
        return Math.max(0, ChronoUnit.DAYS.between(borrowTime.toLocalDate(), returnTime.toLocalDate()));
    }

    public long overdueDaysUntilToday() {
        if (!isActive()) {
            return 0;
        }
        long diff = LocalDate.now().toEpochDay() - dueDate.toEpochDay();
        return Math.max(0, diff);
    }
}
