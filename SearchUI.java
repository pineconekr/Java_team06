package org.example.ui;

import org.example.model.Book;
import org.example.service.SearchService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SearchUI extends JFrame {

    private final SearchService svc =
            new SearchService();

    private JTextField searchField;

    private JComboBox<String> searchTypeBox;

    private JComboBox<String> statusBox;

    private JTable table;

    private DefaultTableModel tableModel;

    public SearchUI() {

        setTitle("도서 검색 시스템");

        setSize(900, 600);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));

        add(buildTopPanel(), BorderLayout.NORTH);

        add(buildTablePanel(), BorderLayout.CENTER);

        setLocationRelativeTo(null);

        setVisible(true);
    }

    private JPanel buildTopPanel() {

        JPanel panel = new JPanel();

        searchField = new JTextField(20);

        searchTypeBox =
                new JComboBox<>(new String[] {
                        "전체",
                        "제목",
                        "저자",
                        "출판사"
                });

        statusBox =
                new JComboBox<>(new String[] {
                        "전체",
                        "AVAILABLE",
                        "BORROWED"
                });

        JButton searchButton =
                new JButton("검색");

        searchButton.addActionListener(
                e -> searchBooks()
        );

        searchField.addActionListener(
                e -> searchBooks()
        );

        panel.add(new JLabel("검색어"));

        panel.add(searchField);

        panel.add(searchTypeBox);

        panel.add(statusBox);

        panel.add(searchButton);

        return panel;
    }

    private JScrollPane buildTablePanel() {

        String[] columns = {
                "ISBN",
                "제목",
                "저자",
                "출판사",
                "출판년도",
                "상태"
        };

        tableModel =
                new DefaultTableModel(columns, 0) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        table = new JTable(tableModel);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        return new JScrollPane(table);
    }

    private void searchBooks() {

        String keyword =
                searchField.getText().trim();

        String searchType =
                (String) searchTypeBox.getSelectedItem();

        String status =
                (String) statusBox.getSelectedItem();

        List<Book> books =
                svc.search(
                        keyword,
                        searchType,
                        status
                );

        tableModel.setRowCount(0);

        for (Book book : books) {

            tableModel.addRow(new Object[] {

                    book.getIsbn(),

                    book.getTitle(),

                    book.getAuthor(),

                    book.getPublisher(),

                    book.getPublishYear(),

                    book.getStatus()
            });
        }

        if (books.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "검색 결과가 없습니다."
            );
        }
    }
}