package org.example.dao;

import org.example.database.DBConnection;
import org.example.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public void insertBook(Book book) {

        String sql = """
                INSERT IGNORE INTO books
                (
                    isbn,
                    title,
                    author,
                    publisher,
                    publish_year,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, book.getIsbn());

            pstmt.setString(2, book.getTitle());

            pstmt.setString(3, book.getAuthor());

            pstmt.setString(4, book.getPublisher());

            pstmt.setString(
                    5,
                    book.getPublishYear()
            );

            pstmt.setString(
                    6,
                    book.getStatus()
            );

            pstmt.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public List<Book> searchBooks(
            String keyword,
            String searchType,
            String status
    ) {

        List<Book> list =
                new ArrayList<>();

        StringBuilder sql =
                new StringBuilder(
                        "SELECT * FROM books WHERE 1=1 "
                );

        List<String> params =
                new ArrayList<>();

        if (!keyword.isEmpty()) {

            switch (searchType) {

                case "제목":

                    sql.append(
                            "AND title LIKE ? "
                    );

                    params.add(
                            "%" + keyword + "%"
                    );

                    break;

                case "저자":

                    sql.append(
                            "AND author LIKE ? "
                    );

                    params.add(
                            "%" + keyword + "%"
                    );

                    break;

                case "출판사":

                    sql.append(
                            "AND publisher LIKE ? "
                    );

                    params.add(
                            "%" + keyword + "%"
                    );

                    break;

                default:

                    sql.append("""
                            AND (
                                title LIKE ?
                                OR author LIKE ?
                                OR publisher LIKE ?
                            )
                            """);

                    params.add(
                            "%" + keyword + "%"
                    );

                    params.add(
                            "%" + keyword + "%"
                    );

                    params.add(
                            "%" + keyword + "%"
                    );
            }
        }

        if (!status.equals("전체")) {

            sql.append(
                    "AND status = ? "
            );

            params.add(status);
        }

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement pstmt =
                        conn.prepareStatement(
                                sql.toString()
                        )
        ) {

            for (int i = 0; i < params.size(); i++) {

                pstmt.setString(
                        i + 1,
                        params.get(i)
                );
            }

            try (
                    ResultSet rs =
                            pstmt.executeQuery()
            ) {

                while (rs.next()) {

                    Book book = new Book();

                    book.setIsbn(
                            rs.getString("isbn")
                    );

                    book.setTitle(
                            rs.getString("title")
                    );

                    book.setAuthor(
                            rs.getString("author")
                    );

                    book.setPublisher(
                            rs.getString("publisher")
                    );

                    book.setPublishYear(
                            rs.getString("publish_year")
                    );

                    book.setStatus(
                            rs.getString("status")
                    );

                    list.add(book);
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return list;
    }
}