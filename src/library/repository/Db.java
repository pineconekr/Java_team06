package library.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * SQLite 연결 + 스키마 초기화 + 최초 1회 시드 데이터 적재를 담당한다.
 * 의존성: lib/sqlite-jdbc-*.jar
 *
 * 단일 사용자 데스크톱 앱이므로 메서드마다 연결을 열고 닫는다(파일 DB라 비용 작음).
 */
public class Db {

    private final String url;

    public Db(String dbFilePath) {
        this.url = "jdbc:sqlite:" + dbFilePath;
        initSchema();
        seedIfEmpty();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void initSchema() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
            s.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    isbn     TEXT PRIMARY KEY,
                    title    TEXT NOT NULL,
                    author   TEXT,
                    category TEXT,
                    status   TEXT NOT NULL DEFAULT 'AVAILABLE'
                )""");
            s.execute("""
                CREATE TABLE IF NOT EXISTS members (
                    member_id       TEXT PRIMARY KEY,
                    name            TEXT NOT NULL,
                    grade           TEXT NOT NULL DEFAULT 'REGULAR',
                    borrow_count    INTEGER NOT NULL DEFAULT 0,
                    suspended_until TEXT
                )""");
            s.execute("""
                CREATE TABLE IF NOT EXISTS loans (
                    loan_id     INTEGER PRIMARY KEY,
                    member_id   TEXT NOT NULL,
                    isbn        TEXT NOT NULL,
                    borrow_date TEXT NOT NULL,
                    due_date    TEXT NOT NULL,
                    return_date TEXT
                )""");
        } catch (SQLException e) {
            throw new RuntimeException("DB 스키마 초기화 실패: " + e.getMessage(), e);
        }
    }

    /** 테이블이 비어 있을 때만 데모용 초기 데이터를 넣는다(기존 인메모리 시드와 동일). */
    private void seedIfEmpty() {
        try (Connection c = getConnection()) {
            if (isEmpty(c, "books")) {
                try (Statement s = c.createStatement()) {
                    // book1, book2는 시드 대출과 맞춰 BORROWED 상태로 둔다.
                    s.execute("INSERT INTO books VALUES "
                            + "('978-0-06-112008-4','앵무새 죽이기','하퍼 리','소설','BORROWED'),"
                            + "('978-0-7432-7356-5','1984','조지 오웰','소설','BORROWED'),"
                            + "('978-0-345-80301-8','클린 코드','로버트 마틴','IT','AVAILABLE')");
                }
            }
            if (isEmpty(c, "members")) {
                try (Statement s = c.createStatement()) {
                    s.execute("INSERT INTO members VALUES "
                            + "('M001','김철수','REGULAR',1,NULL),"
                            + "('M002','이영희','PREMIUM',1,NULL),"
                            + "('M003','박민준','REGULAR',0,NULL)");
                }
            }
            if (isEmpty(c, "loans")) {
                String today = LocalDate.now().toString();
                String due = LocalDate.now().plusDays(14).toString();
                try (Statement s = c.createStatement()) {
                    s.execute("INSERT INTO loans VALUES "
                            + "(1,'M001','978-0-06-112008-4','" + today + "','" + due + "',NULL),"
                            + "(2,'M002','978-0-7432-7356-5','" + today + "','" + due + "',NULL)");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB 시드 적재 실패: " + e.getMessage(), e);
        }
    }

    private boolean isEmpty(Connection c, String table) throws SQLException {
        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return r.next() && r.getInt(1) == 0;
        }
    }
}
