package library.service;

import library.model.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// 도서 검색 서비스 (인메모리)
public class SearchSvc {

    private final List<Book> books = new ArrayList<>();

    public void addBook(Book book) {
        books.add(book);
    }

    public List<Book> searchByTitle(String keyword) {
        return filter(b -> b.getTitle().contains(keyword));
    }

    public List<Book> searchByAuthor(String keyword) {
        return filter(b -> b.getAuthor().contains(keyword));
    }

    public List<Book> searchByIsbn(String isbn) {
        return filter(b -> b.getIsbn().equals(isbn));
    }

    public List<Book> searchByCategory(String category) {
        return filter(b -> b.getCategory().contains(category));
    }

    public List<Book> searchAll(String keyword) {
        return filter(b ->
                b.getTitle().contains(keyword) ||
                b.getAuthor().contains(keyword) ||
                b.getIsbn().contains(keyword) ||
                b.getCategory().contains(keyword));
    }

    private List<Book> filter(java.util.function.Predicate<Book> predicate) {
        return books.stream().filter(predicate).collect(Collectors.toList());
    }
}
