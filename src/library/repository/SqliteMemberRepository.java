package library.repository;

import library.model.Member;
import library.model.MemberGrade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * IMemberRepository의 SQLite 구현.
 * add()는 INSERT OR REPLACE(upsert)로 동작하여, 신규 등록과 대출 권수/정지일 변경 반영을 모두 처리한다.
 */
public class SqliteMemberRepository implements IMemberRepository {

    private final Db db;

    public SqliteMemberRepository(Db db) {
        this.db = db;
    }

    @Override
    public void add(Member member) {
        String sql = "INSERT OR REPLACE INTO members(member_id, name, grade, borrow_count, suspended_until) VALUES(?,?,?,?,?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, member.getMemberId());
            ps.setString(2, member.getName());
            ps.setString(3, member.getGrade() == null ? MemberGrade.REGULAR.name() : member.getGrade().name());
            ps.setInt(4, member.getCurrentBorrowCount());
            ps.setString(5, member.getSuspendedUntil() == null ? null : member.getSuspendedUntil().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("회원 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Member> findById(String memberId) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM members WHERE member_id = ?")) {
            ps.setString(1, memberId);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("회원 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Member> findAll() {
        List<Member> list = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM members ORDER BY rowid");
             ResultSet r = ps.executeQuery()) {
            while (r.next()) list.add(map(r));
        } catch (SQLException e) {
            throw new RuntimeException("회원 목록 조회 실패: " + e.getMessage(), e);
        }
        return list;
    }

    private Member map(ResultSet r) throws SQLException {
        Member member = new Member(r.getString("member_id"), r.getString("name"));
        String grade = r.getString("grade");
        member.setGrade(grade == null ? MemberGrade.REGULAR : MemberGrade.valueOf(grade));
        member.setCurrentBorrowCount(r.getInt("borrow_count"));
        String suspended = r.getString("suspended_until");
        member.setSuspendedUntil(suspended == null ? null : LocalDate.parse(suspended));
        return member;
    }
}
