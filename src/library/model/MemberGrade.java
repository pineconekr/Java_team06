package library.model;
public enum MemberGrade {
    REGULAR(3), PREMIUM(5);
    private final int max;
    MemberGrade(int max) { this.max = max; }
    public int getMaxBorrowCount() { return max; }
}
