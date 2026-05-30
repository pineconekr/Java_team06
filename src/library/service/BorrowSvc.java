package library.service;

import library.model.*;
import library.repository.*;

public class BorrowSvc {

    // 저장소(Repository) 인터페이스 연결
    private IBookRepository bookRepo;
    private IMemberRepository memberRepo;
    private ILoanRepository loanRepo;

    // 생성자를 통해 메인 화면 구동 시 실제 저장소 객체들을 주입
    public BorrowSvc(IBookRepository bookRepo, IMemberRepository memberRepo, ILoanRepository loanRepo) {
        this.bookRepo = bookRepo;
        this.memberRepo = memberRepo;
        this.loanRepo = loanRepo;
    }

    /**
     * [대출 가능 여부 확인]
     * @param memberId 검사할 회원의 아이디
     * @return 대출 가능하면 true, 불가능하면 false
     */
    public boolean canBorrow(String memberId) {
        // 리포지토리 문법에 맞춰 .orElse(null)로 안전하게 꺼냄
        Member member = memberRepo.findById(memberId).orElse(null);

        if (member == null) return false;

        // 1. 연체 및 정지 상태 확인 (Member.java 내장 기능 활용)
        if (member.isSuspended()) return false;

        // 2. 등급별 대출 권수 제한 확인 (REGULAR 3권, PREMIUM 5권)
        int maxLimit = (member.getGrade() == MemberGrade.PREMIUM) ? 5 : 3;
        if (member.getCurrentBorrowCount() >= maxLimit) return false;

        return true; // 모두 통과 시 대출 가능
    }

    /**
     * [실제 대출 처리 로직]
     * @param memberId 대출할 회원의 아이디
     * @param isbn 대출할 도서의 ISBN 번호
     * @return 대출 처리 결과 상태값 (BorrowResult Enum)
     */
    public BorrowResult borrow(String memberId, String isbn) {
        // 1. 회원 정보 조회 및 존재 여부 확인
        Member member = memberRepo.findById(memberId).orElse(null);
        if (member == null) {
            return BorrowResult.FAIL_NOT_AVAILABLE; // 회원이 없으면 대출 불가
        }

        // 2. 연체 정지 상태 검사
        if (member.isSuspended()) {
            return BorrowResult.FAIL_SUSPENDED; // 정확하게 연체 정지 실패 리턴
        }

        // 3. 등급별 권수 한도 초과 제한 검사
        int maxLimit = (member.getGrade() == MemberGrade.PREMIUM) ? 5 : 3;
        if (member.getCurrentBorrowCount() >= maxLimit) {
            return BorrowResult.FAIL_LIMIT_EXCEEDED; // 정확하게 한도 초과 실패 리턴!
        }

        // 4. 도서 정보 조회 및 상태 검사
        Book book = bookRepo.findByIsbn(isbn).orElse(null);
        if (book == null || book.getStatus() != BookStatus.AVAILABLE) {
            return BorrowResult.FAIL_NOT_AVAILABLE; // 도서가 없거나 이미 대출 중인 경우
        }

        // 5. 검증 완료 후 객체 상태 업데이트
        book.setStatus(BookStatus.BORROWED); // 도서 상태를 대출 중으로 변경
        member.setCurrentBorrowCount(member.getCurrentBorrowCount() + 1); // 회원의 대출 권수 +1

        // 5-1. 변경된 상태를 저장소에 반영(write-through).
        //      인메모리는 같은 객체라 무해, DB 저장소는 이 호출로 영구 반영된다.
        bookRepo.add(book);
        memberRepo.add(member);

        // 6. 대출 장부(Loan) 생성 및 기록 저장
        // memberid , isbn사용
        Loan newLoan = new Loan(memberId, isbn);
        loanRepo.save(newLoan); // 대출 이력 저장소에 기록 보관

        System.out.println("대출 성공 처리 완료! 반납 예정일: " + newLoan.getDueDate());
        return BorrowResult.SUCCESS; // 최종 성공 반환
    }
}
