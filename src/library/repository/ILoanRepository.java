package library.repository;

import library.model.Loan;
import java.util.List;
import java.util.Optional;

public interface ILoanRepository {
    void save(Loan loan);
    Optional<Loan> findById(int loanId);
    List<Loan> findByMemberId(String memberId);
    List<Loan> findActiveLoansByMemberId(String memberId);
    Optional<Loan> findActiveByIsbn(String isbn);
    List<Loan> findOverdueLoans();
}
