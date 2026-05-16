package org.example.service;

import org.example.dao.BookDAO;
import org.example.model.Book;

import java.util.List;

public class SearchService {

    private final BookDAO dao = new BookDAO();

    public List<Book> search(
        String keyword,
        String searchType,
        String status) {

        return dao.searchBooks(keyword, searchType, status);
    }
}