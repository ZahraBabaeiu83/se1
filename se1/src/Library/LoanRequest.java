package Library;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

class LoanRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final int studentId;
    private final int bookId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDateTime requestTime;

    private RequestStatus status = RequestStatus.PENDING;
    private String reviewerUsername;
    private LocalDateTime reviewTime;

    public LoanRequest(int id, int studentId, int bookId,
                       LocalDate startDate, LocalDate endDate, LocalDateTime requestTime) {
        this.id = id;
        this.studentId = studentId;
        this.bookId = bookId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.requestTime = requestTime;
    }

    public int getId() {
        return id;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getBookId() {
        return bookId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public void setReview(String reviewerUsername, LocalDateTime reviewTime) {
        this.reviewerUsername = reviewerUsername;
        this.reviewTime = reviewTime;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }
}
