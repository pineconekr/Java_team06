package library.repository;

import library.model.Loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ILoanRepository의 SQLite 구현.
 * save()는 loan_id 기준 INSERT OR REPLACE(upsert)로, 신규 대출과 반납일 갱신을 모두 처리한다.
 * 생성 시 DB의 최대 loan_id로 Loan 발급 카운터를 동기화한다.
 */
public class SqliteLoanRepository implements ILoanRepository {

    private final Db db;

    public SqliteLoanRepository(Db db) {
        this.db = db;
        syncIdCounter();
    }

    /** DB에 저장된 최대 loan_id 이후로 신규 발급 id가 매겨지도록 맞춘다. */
    private void syncIdCounter() {
        try (Connection c = db.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COALESCE(MAX(loan_id), 0) FROM loans")) {
            if (r.next()) Loan.syncIdCounter(r.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException("대출 id 카운터 동기화 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Loan loan) {
        String sql = "INSERT OR REPLACE INTO loans(loan_id, member_id, isbn, borrow_date, due_date, return_date) VALUES(?,?,?,?,?,?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, loan.getLoanId());
            ps.setString(2, loan.getMemberId());
            ps.setString(3, loan.getIsbn());
            ps.setString(4, loan.getBorrowDate().toString());
            ps.setString(5, loan.getDueDate().toString());
            ps.setString(6, loan.getReturnDate() == null ? null : loan.getReturnDate().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("대출 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Loan> findById(int loanId) {
        return queryOne("SELECT * FROM loans WHERE loan_id = ?", ps -> ps.setInt(1, loanId));
    }

    @Override
    public List<Loan> findByMemberId(String memberId) {
        return queryList("SELECT * FROM loans WHERE member_id = ? ORDER BY loan_id",
                ps -> ps.setString(1, memberId));
    }

    @Override
    public List<Loan> findActiveLoansByMemberId(String memberId) {
        return queryList("SELECT * FROM loans WHERE member_id = ? AND return_date IS NULL ORDER BY loan_id",
                ps -> ps.setString(1, memberId));
    }

    @Override
    public Optional<Loan> findActiveByIsbn(String isbn) {
        return queryOne("SELECT * FROM loans WHERE isbn = ? AND return_date IS NULL ORDER BY loan_id LIMIT 1",
                ps -> ps.setString(1, isbn));
    }

    @Override
    public List<Loan> findOverdueLoans() {
        // 미반납 + 반납기한이 오늘 이전(ISO 날짜 문자열은 사전식 비교가 곧 날짜 비교).
        String today = LocalDate.now().toString();
        return queryList("SELECT * FROM loans WHERE return_date IS NULL AND due_date < ? ORDER BY loan_id",
                ps -> ps.setString(1, today));
    }

    // ---- 공통 헬퍼 ----

    private interface Binder { void bind(PreparedStatement ps) throws SQLException; }

    private Optional<Loan> queryOne(String sql, Binder binder) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("대출 조회 실패: " + e.getMessage(), e);
        }
    }

    private List<Loan> queryList(String sql, Binder binder) {
        List<Loan> list = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) list.add(map(r));
            }
        } catch (SQLException e) {
            throw new RuntimeException("대출 목록 조회 실패: " + e.getMessage(), e);
        }
        return list;
    }

    private Loan map(ResultSet r) throws SQLException {
        String returnDate = r.getString("return_date");
        return new Loan(
                r.getInt("loan_id"),
                r.getString("member_id"),
                r.getString("isbn"),
                LocalDate.parse(r.getString("borrow_date")),
                LocalDate.parse(r.getString("due_date")),
                returnDate == null ? null : LocalDate.parse(returnDate));
    }
}
