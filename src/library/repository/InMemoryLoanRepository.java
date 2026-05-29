package library.repository;

import library.model.Loan;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryLoanRepository implements ILoanRepository {
    private static final InMemoryLoanRepository INSTANCE = new InMemoryLoanRepository();
    private final List<Loan> loans = new ArrayList<>();

    private InMemoryLoanRepository() {
        loans.add(new Loan("M001", "978-0-06-112008-4"));
        loans.add(new Loan("M002", "978-0-7432-7356-5"));
    }

    public static InMemoryLoanRepository getInstance() { return INSTANCE; }

    @Override public void save(Loan l) {
        loans.removeIf(x -> x.getLoanId() == l.getLoanId()); // 같은 id 있으면 교체(upsert)
        loans.add(l);
    }
    @Override public Optional<Loan> findById(int id) { return loans.stream().filter(l -> l.getLoanId() == id).findFirst(); }
    @Override public List<Loan> findByMemberId(String id) { return loans.stream().filter(l -> l.getMemberId().equals(id)).collect(Collectors.toList()); }
    @Override public List<Loan> findActiveLoansByMemberId(String id) { return loans.stream().filter(l -> l.getMemberId().equals(id) && !l.isReturned()).collect(Collectors.toList()); }
    @Override public Optional<Loan> findActiveByIsbn(String isbn) { return loans.stream().filter(l -> l.getIsbn().equals(isbn) && !l.isReturned()).findFirst(); }
    @Override public List<Loan> findOverdueLoans() { return loans.stream().filter(l -> !l.isReturned() && l.isOverdue()).collect(Collectors.toList()); }
}
