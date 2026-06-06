package library.repository;

import library.model.Book;
import library.model.BookStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * IBookRepository의 SQLite 구현.
 * add()는 INSERT OR REPLACE(upsert)로 동작하여, 신규 등록과 상태 변경 반영을 모두 처리한다.
 */
public class SqliteBookRepository implements IBookRepository {

    private final Db db;

    public SqliteBookRepository(Db db) {
        this.db = db;
    }

    @Override
    public void add(Book book) {
        String sql = "INSERT OR REPLACE INTO books(isbn, title, author, category, status, cover_url) VALUES(?,?,?,?,?,?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getCategory());
            ps.setString(5, book.getStatus() == null ? BookStatus.AVAILABLE.name() : book.getStatus().name());
            ps.setString(6, book.getCoverUrl());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("도서 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String isbn) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM books WHERE isbn = ?")) {
            ps.setString(1, isbn);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("도서 삭제 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM books WHERE isbn = ?")) {
            ps.setString(1, isbn);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("도서 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM books ORDER BY rowid");
             ResultSet r = ps.executeQuery()) {
            while (r.next()) list.add(map(r));
        } catch (SQLException e) {
            throw new RuntimeException("도서 목록 조회 실패: " + e.getMessage(), e);
        }
        return list;
    }

    private Book map(ResultSet r) throws SQLException {
        Book book = new Book(
                r.getString("isbn"),
                r.getString("title"),
                r.getString("author"),
                r.getString("category"),
                r.getString("cover_url"));
        String status = r.getString("status");
        book.setStatus(status == null ? BookStatus.AVAILABLE : BookStatus.valueOf(status));
        return book;
    }
}
