package library.service;

import library.model.Book;
import library.model.BookStatus;
import library.model.Loan;
import library.model.Member;
import library.repository.BookRepository;
import library.repository.LoanRepository;
import library.repository.MemberRepository;

import java.time.LocalDate;
import java.util.Optional;

// 도서 반납 처리 및 연체 패널티 적용 (패널티: 연체일 x 2일 대출 정지)
public class ReturnSvc {

    public enum ReturnResult {
        SUCCESS,
        SUCCESS_PENALTY,
        LOAN_NOT_FOUND,
        ALREADY_RETURNED
    }

    public static class ReturnInfo {
        public final ReturnResult result;
        public final Loan loan;
        public final long overdueDays;
        public final long penaltyDays;
        public final LocalDate suspendedUntil;

        public ReturnInfo(ReturnResult result, Loan loan,
                          long overdueDays, long penaltyDays,
                          LocalDate suspendedUntil) {
            this.result = result;
            this.loan = loan;
            this.overdueDays = overdueDays;
            this.penaltyDays = penaltyDays;
            this.suspendedUntil = suspendedUntil;
        }
    }

    private final LoanRepository loanRepo = LoanRepository.getInstance();
    private final BookRepository bookRepo = BookRepository.getInstance();
    private final MemberRepository memberRepo = MemberRepository.getInstance();

    public ReturnInfo returnBook(int loanId) {
        Optional<Loan> loanOpt = loanRepo.findById(loanId);
        if (loanOpt.isEmpty())
            return new ReturnInfo(ReturnResult.LOAN_NOT_FOUND, null, 0, 0, null);

        Loan loan = loanOpt.get();
        if (loan.isReturned())
            return new ReturnInfo(ReturnResult.ALREADY_RETURNED, loan, 0, 0, null);

        LocalDate today = LocalDate.now();
        loan.setReturnDate(today);

        bookRepo.findByIsbn(loan.getIsbn())
                .ifPresent(book -> book.setStatus(BookStatus.AVAILABLE));

        Optional<Member> memberOpt = memberRepo.findById(loan.getMemberId());
        if (memberOpt.isEmpty())
            return new ReturnInfo(ReturnResult.LOAN_NOT_FOUND, loan, 0, 0, null);

        Member member = memberOpt.get();
        member.setCurrentBorrowCount(Math.max(0, member.getCurrentBorrowCount() - 1));

        long overdueDays = loan.getOverdueDays();
        long penaltyDays = loan.getPenaltyDays();

        if (penaltyDays > 0) {
            LocalDate base = member.isSuspended() ? member.getSuspendedUntil() : today;
            LocalDate suspendedUntil = base.plusDays(penaltyDays);
            member.setSuspendedUntil(suspendedUntil);
            return new ReturnInfo(ReturnResult.SUCCESS_PENALTY, loan,
                    overdueDays, penaltyDays, suspendedUntil);
        }

        return new ReturnInfo(ReturnResult.SUCCESS, loan, 0, 0, null);
    }

    public ReturnInfo returnByIsbn(String isbn) {
        Optional<Loan> loanOpt = loanRepo.findActiveByIsbn(isbn);
        if (loanOpt.isEmpty())
            return new ReturnInfo(ReturnResult.LOAN_NOT_FOUND, null, 0, 0, null);
        return returnBook(loanOpt.get().getLoanId());
    }
}
