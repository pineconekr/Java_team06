package library.service;

import library.model.Book;
import library.model.Loan;
import library.repository.BookRepository;
import library.repository.LoanRepository;

import java.util.List;

// 대출 이력 조회 서비스
public class HistorySvc {

    private final LoanRepository loanRepo = LoanRepository.getInstance();
    private final BookRepository bookRepo = BookRepository.getInstance();

    public List<Loan> getHistory(String memberId) {
        return loanRepo.findByMemberId(memberId);
    }

    public List<Loan> getActive(String memberId) {
        return loanRepo.findActiveLoansByMemberId(memberId);
    }

    public List<Loan> getOverdue() {
        return loanRepo.findOverdueLoans();
    }

    public String getBookTitle(Loan loan) {
        return bookRepo.findByIsbn(loan.getIsbn())
                .map(Book::getTitle)
                .orElse("(알 수 없음)");
    }

    public Object[] toRow(Loan loan) {
        String status;
        if (loan.isReturned())          status = "반납 완료";
        else if (loan.isOverdue())      status = "연체 중 (" + loan.getOverdueDays() + "일)";
        else                            status = "대출 중";

        return new Object[]{
                loan.getLoanId(),
                getBookTitle(loan),
                loan.getIsbn(),
                loan.getBorrowDate(),
                loan.getDueDate(),
                loan.isReturned() ? loan.getReturnDate() : "-",
                status
        };
    }
}
