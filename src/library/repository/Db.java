package library.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite 데이터베이스 관리 클래스
 * 테이블 스키마 생성 및 초기 시드 데이터 적재 담당
 */
public class Db {

    private final String url;

    public Db(String dbFilePath) {
        this.url = "jdbc:sqlite:" + dbFilePath;
        initSchema();
        seedBooks();
        seedMembers();
        seedLoans();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void initSchema() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");

            // 1. 도서 테이블
            s.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    isbn     TEXT PRIMARY KEY,
                    title    TEXT NOT NULL,
                    author   TEXT,
                    category TEXT,
                    status   TEXT NOT NULL DEFAULT 'AVAILABLE'
                )""");

            // 2. 회원 테이블
            s.execute("""
                CREATE TABLE IF NOT EXISTS members (
                    member_id       TEXT PRIMARY KEY,
                    name            TEXT NOT NULL,
                    grade           TEXT NOT NULL,
                    borrow_count    INTEGER DEFAULT 0,
                    suspended_until TEXT
                )""");

            // 3. 대출 테이블
            s.execute("""
                CREATE TABLE IF NOT EXISTS loans (
                    loan_id     INTEGER PRIMARY KEY,
                    member_id   TEXT NOT NULL,
                    isbn        TEXT NOT NULL,
                    borrow_date TEXT NOT NULL,
                    due_date    TEXT NOT NULL,
                    return_date TEXT,
                    FOREIGN KEY(member_id) REFERENCES members(member_id),
                    FOREIGN KEY(isbn) REFERENCES books(isbn)
                )""");
        } catch (SQLException e) {
            throw new RuntimeException("DB 스키마 초기화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 도서 시드 데이터를 INSERT OR IGNORE로 적재한다.
     * 이미 존재하는 ISBN은 건너뛰므로 기존 DB에 도서가 있어도 안전하게
     * 누락된 항목만 추가된다.
     */
    private void seedBooks() {
        String[][] books = {
                {"978-0-06-112008-4", "앵무새 죽이기", "하퍼 리", "소설"},
                {"978-0-7432-7356-5", "1984", "조지 오웰", "소설"},
                {"978-89-364-3412-1", "아몬드",                    "손원평",         "소설"},
                {"9791194891116",     "혐오도 복제가 되나요",       "윤혜성",         "소설"},
                {"9791167376237",     "카프네",                    "아베 아키코",     "소설"},
                {"9791199206557",     "오시 하나, 내 멋대로 산다", "우치다테 마키코", "소설"},
                {"9791160408331",     "트로피컬 나이트",            "조예은",         "소설"},
                {"INTERNAL-001",      "강물이 멈춘 날",             "월리 램",        "소설"},
                {"INTERNAL-002",      "내 심장을 쏴라",             "정유정",         "소설"},
                {"9791168343764",     "흉담",                      "전건우",         "소설"},
                {"978-0-345-80301-8", "클린 코드",                 "로버트 마틴",    "IT"},
                {"9791141602468", "사랑의 힘", "박서련", "소설"},
                {"9791199305304", "자몽살구클럽", "한로로", "소설"},
                {"9788970134796", "은하수를 여행하는 히치하이커를 위한 안내서. 1", "더글러스 애덤스", "소설"},
                {"8970134808",    "은하수를 여행하는 히치하이커를 위한 안내서. 2", "더글러스 애덤스", "소설"},
                {"8970134815",    "은하수를 여행하는 히치하이커를 위한 안내서. 3", "더글러스 애덤스", "소설"},
                {"8970134913",    "은하수를 여행하는 히치하이커를 위한 안내서. 4", "더글러스 애덤스", "소설"},
                {"8970134921",    "은하수를 여행하는 히치하이커를 위한 안내서. 5", "더글러스 애덤스", "소설"},
                {"9788970137476", "은하수를 여행하는 히치하이커를 위한 안내서. 6", "이오인 콜퍼", "소설"}
        };

        String sql = "INSERT OR IGNORE INTO books(isbn, title, author, category, status) VALUES(?,?,?,?,'AVAILABLE')";
        try (Connection c = getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            for (String[] b : books) {
                ps.setString(1, b[0]);
                ps.setString(2, b[1]);
                ps.setString(3, b[2]);
                ps.setString(4, b[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("도서 시드 데이터 적재 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 회원 시드 데이터를 INSERT OR IGNORE로 적재한다.
     */
    private void seedMembers() {
        String[][] members = {
                {"M001", "김철수", "REGULAR"},
                {"M002", "이영희", "PREMIUM"},
                {"M003", "박민준", "REGULAR"},
        };

        String sql = "INSERT OR IGNORE INTO members(member_id, name, grade, borrow_count, suspended_until) VALUES(?,?,?,0,NULL)";
        try (Connection c = getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            for (String[] m : members) {
                ps.setString(1, m[0]);
                ps.setString(2, m[1]);
                ps.setString(3, m[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("회원 시드 데이터 적재 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 대출 시드 데이터를 INSERT OR IGNORE로 적재한다.
     * loan_id가 PRIMARY KEY이므로 같은 id가 이미 있으면 건너뛴다.
     */
    private void seedLoans() {
        String today = java.time.LocalDate.now().toString();
        String due   = java.time.LocalDate.now().plusDays(14).toString();

        String sql = "INSERT OR IGNORE INTO loans(loan_id, member_id, isbn, borrow_date, due_date, return_date) VALUES(?,?,?,?,?,NULL)";
        try (Connection c = getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {

            // 대출 1: M001 - 978-0-06-112008-4
            ps.setInt(1, 1);
            ps.setString(2, "M001");
            ps.setString(3, "978-0-06-112008-4");
            ps.setString(4, today);
            ps.setString(5, due);
            ps.addBatch();

            // 대출 2: M002 - 978-0-7432-7356-5
            ps.setInt(1, 2);
            ps.setString(2, "M002");
            ps.setString(3, "978-0-7432-7356-5");
            ps.setString(4, today);
            ps.setString(5, due);
            ps.addBatch();

            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("대출 시드 데이터 적재 실패: " + e.getMessage(), e);
        }
    }

    private boolean isEmpty(Connection c, String tableName) throws SQLException {
        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return r.next() && r.getInt(1) == 0;
        }
    }
}