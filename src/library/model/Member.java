package library.model;
import java.time.LocalDate;
public class Member {
    private String memberId, name;
    private MemberGrade grade = MemberGrade.REGULAR;
    private int borrowCount = 0;
    private LocalDate suspendedUntil = null;
    public Member(String memberId, String name) { this.memberId = memberId; this.name = name; }
    public String getMemberId()          { return memberId; }
    public String getName()              { return name; }
    public MemberGrade getGrade()        { return grade; }
    public int getCurrentBorrowCount()   { return borrowCount; }
    public LocalDate getSuspendedUntil() { return suspendedUntil; }
    public void setGrade(MemberGrade g)          { this.grade = g; }
    public void setCurrentBorrowCount(int c)     { this.borrowCount = c; }
    public void setSuspendedUntil(LocalDate d)   { this.suspendedUntil = d; }
    public boolean isSuspended() {
        return suspendedUntil != null && LocalDate.now().isBefore(suspendedUntil);
    }
}
