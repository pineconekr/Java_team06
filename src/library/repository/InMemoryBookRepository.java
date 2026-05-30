package library.repository;

import library.model.Book;
import java.util.*;

public class InMemoryBookRepository implements IBookRepository {
    private static final InMemoryBookRepository INSTANCE = new InMemoryBookRepository();
    private final Map<String, Book> books = new LinkedHashMap<>();

    private InMemoryBookRepository() {
        add(new Book("978-0-06-112008-4", "앵무새 죽이기", "하퍼 리", "소설"));
        add(new Book("978-0-7432-7356-5", "1984", "조지 오웰", "소설"));
        add(new Book("978-0-345-80301-8", "클린 코드", "로버트 마틴", "IT"));
    }

    public static InMemoryBookRepository getInstance() { return INSTANCE; }

    @Override public void add(Book book) { books.put(book.getIsbn(), book); }
    @Override public boolean delete(String isbn) { return books.remove(isbn) != null; }
    @Override public Optional<Book> findByIsbn(String isbn) { return Optional.ofNullable(books.get(isbn)); }
    @Override public List<Book> findAll() { return new ArrayList<>(books.values()); }
}
