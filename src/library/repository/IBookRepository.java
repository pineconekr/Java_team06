package library.repository;

import library.model.Book;
import java.util.List;
import java.util.Optional;

public interface IBookRepository {
    void add(Book book);
    Optional<Book> findByIsbn(String isbn);
    List<Book> findAll();
}
