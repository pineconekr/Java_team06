package library.model;

public enum BorrowResult {
    SUCCESS,             // 대출 성공
    FAIL_SUSPENDED,       // 실패: 연체 정지 상태 회원
    FAIL_LIMIT_EXCEEDED,  // 실패: 대출 권수 한도 초과
    FAIL_NOT_AVAILABLE    // 실패: 책이 이미 대출 중이거나 없음
}
