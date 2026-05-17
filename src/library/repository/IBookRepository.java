package library.repository;

import library.model.Book;
import java.util.List;
import java.util.Optional;

public interface IBookRepository {
    void add(Book book);
    boolean delete(String isbn);
    Optional<Book> findByIsbn(String isbn);
    List<Book> findAll();
}
