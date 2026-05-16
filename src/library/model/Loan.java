package library.model;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
public class Loan {
    private static int idCounter = 1;
    private int loanId;
    private String memberId, isbn;
    private LocalDate borrowDate, dueDate, returnDate;
    public static final int LOAN_PERIOD_DAYS = 14;
    public Loan(String memberId, String isbn) {
        this.loanId = idCounter++;
        this.memberId = memberId; this.isbn = isbn;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(LOAN_PERIOD_DAYS);
    }
    public int getLoanId()           { return loanId; }
    public String getMemberId()      { return memberId; }
    public String getIsbn()          { return isbn; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate()    { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate d) { this.returnDate = d; }
    public boolean isReturned() { return returnDate != null; }
    public boolean isOverdue() {
        LocalDate check = isReturned() ? returnDate : LocalDate.now();
        return check.isAfter(dueDate);
    }
    public long getOverdueDays() {
        if (!isOverdue()) return 0;
        LocalDate check = isReturned() ? returnDate : LocalDate.now();
        return ChronoUnit.DAYS.between(dueDate, check);
    }
    public long getPenaltyDays() { return getOverdueDays() * 2; }
}
