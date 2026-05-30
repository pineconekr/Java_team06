package library.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

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

            // 1. 도서 테이블
            s.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    isbn     TEXT PRIMARY KEY,
                    title    TEXT NOT NULL,
                    author   TEXT,
                    category TEXT,
                    status   TEXT NOT NULL DEFAULT 'AVAILABLE'
                )""");

            // 2. 회원 테이블 (기존 로직 유지 및 확장 가능)
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

    private void seedIfEmpty() {
        try (Connection c = getConnection()) {
            // 도서 데이터 초기화
            if (isEmpty(c, "books")) {
                try (Statement s = c.createStatement()) {
                    s.execute("INSERT INTO books VALUES "
                            + "('978-0-06-112008-4','앵무새 죽이기','하퍼 리','소설','BORROWED'),"
                            + "('978-0-7432-7356-5','1984','조지 오웰','소설','BORROWED'),"
                            + "('978-0-345-80301-8','클린 코드','로버트 마틴','IT','AVAILABLE')");
                }
            }

            // 회원 데이터 초기화
            if (isEmpty(c, "members")) {
                try (Statement s = c.createStatement()) {
                    s.execute("INSERT INTO members VALUES "
                            + "('M001','김철수','REGULAR',1,NULL),"
                            + "('M002','이영희','PREMIUM',1,NULL),"
                            + "('M003','박민준','REGULAR',0,NULL)");
                }
            }

            // 대출 데이터 초기화
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
            throw new RuntimeException("DB 시드 데이터 적재 실패: " + e.getMessage(), e);
        }
    }

    private boolean isEmpty(Connection c, String tableName) throws SQLException {
        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return r.next() && r.getInt(1) == 0;
        }
    }
}