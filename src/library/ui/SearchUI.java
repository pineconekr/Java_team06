package library.ui;

import library.model.Book;
import library.service.SearchSvc;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// 도서 검색 화면
public class SearchUI extends JPanel {

    private final SearchSvc svc = new SearchSvc();

    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> typeBox = new JComboBox<>(
            new String[]{"전체", "제목", "저자", "ISBN", "카테고리"});

    private final String[] COLUMNS = {"ISBN", "제목", "저자", "카테고리", "상태"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public SearchUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.add(new JLabel("검색어:"));
        panel.add(searchField);
        panel.add(typeBox);

        JButton searchBtn = new JButton("검색");
        searchBtn.addActionListener(e -> search());
        searchField.addActionListener(e -> search());
        panel.add(searchBtn);
        return panel;
    }

    private void search() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;

        String type = (String) typeBox.getSelectedItem();
        List<Book> result = switch (type) {
            case "제목"     -> svc.searchByTitle(keyword);
            case "저자"     -> svc.searchByAuthor(keyword);
            case "ISBN"    -> svc.searchByIsbn(keyword);
            case "카테고리" -> svc.searchByCategory(keyword);
            default        -> svc.searchAll(keyword);
        };

        tableModel.setRowCount(0);
        for (Book b : result) {
            tableModel.addRow(new Object[]{
                    b.getIsbn(), b.getTitle(), b.getAuthor(),
                    b.getCategory(), b.getStatus()});
        }

        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this, "검색 결과가 없습니다.");
        }
    }
}
