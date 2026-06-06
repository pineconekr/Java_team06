package library.ui;

import library.model.Book;
import library.service.BookAdminSvc;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// 도서 관리(사서) 화면 - 등록 / 수정 / 삭제 + 국립중앙도서관 OpenAPI 수집
public class BookAdminUI extends JPanel {

    private final BookAdminSvc svc;

    private final JTextField isbnField     = new JTextField(18);
    private final JTextField titleField    = new JTextField(18);
    private final JTextField authorField   = new JTextField(18);
    private final JTextField categoryField = new JTextField(18);

    private final JTextField apiKeywordField = new JTextField(16);

    private final String[] COLUMNS = {"ISBN", "제목", "저자", "카테고리", "상태"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public BookAdminUI(BookAdminSvc svc) {
        this.svc = svc;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        // 행 선택 시 입력란에 자동 채움 (수정/삭제 편의)
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row < 0) return;
            isbnField.setText(str(tableModel.getValueAt(row, 0)));
            titleField.setText(str(tableModel.getValueAt(row, 1)));
            authorField.setText(str(tableModel.getValueAt(row, 2)));
            categoryField.setText(str(tableModel.getValueAt(row, 3)));
        });

        refreshTable();
    }

    private JPanel buildFormPanel() {
        JPanel wrap = new JPanel(new BorderLayout(0, 10));

        // --- 도서 입력 폼 ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new TitledBorder("도서 정보"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 6, 5, 6);
        gc.anchor = GridBagConstraints.WEST;

        addRow(form, gc, 0, "ISBN:",     isbnField);
        addRow(form, gc, 1, "제목:",     titleField);
        addRow(form, gc, 2, "저자:",     authorField);
        addRow(form, gc, 3, "카테고리:", categoryField);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton addBtn    = new JButton("등록");
        JButton updateBtn = new JButton("수정");
        JButton deleteBtn = new JButton("삭제");
        JButton clearBtn  = new JButton("입력 초기화");
        addBtn.addActionListener(e -> onAdd());
        updateBtn.addActionListener(e -> onUpdate());
        deleteBtn.addActionListener(e -> onDelete());
        clearBtn.addActionListener(e -> clearForm());
        btnRow.add(addBtn);
        btnRow.add(updateBtn);
        btnRow.add(deleteBtn);
        btnRow.add(clearBtn);
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        form.add(btnRow, gc);

        // --- API 수집 폼 ---
        JPanel api = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        api.setBorder(new TitledBorder("국립중앙도서관 OpenAPI 수집"));
        api.add(new JLabel("키워드:"));
        api.add(apiKeywordField);
        JButton collectBtn = new JButton("검색해서 일괄 등록");
        collectBtn.addActionListener(e -> onCollect());
        apiKeywordField.addActionListener(e -> onCollect());
        api.add(collectBtn);

        wrap.add(form, BorderLayout.CENTER);
        wrap.add(api, BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("등록된 도서 목록"));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void addRow(JPanel p, GridBagConstraints gc, int y, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = y; gc.gridwidth = 1;
        p.add(new JLabel(label), gc);
        gc.gridx = 1;
        p.add(field, gc);
    }

    private void onAdd() {
        Book book = readForm();
        if (book == null) return;
        if (svc.registerBook(book)) {
            JOptionPane.showMessageDialog(this, "도서가 등록되었습니다.");
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "등록 실패: ISBN이 비었거나 이미 등록된 도서입니다.",
                    "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdate() {
        Book book = readForm();
        if (book == null) return;
        if (svc.updateBook(book.getIsbn(), book)) {
            JOptionPane.showMessageDialog(this, "도서 정보가 수정되었습니다.");
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "수정 실패: 해당 ISBN의 도서가 없습니다.",
                    "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "삭제할 도서의 ISBN을 입력(또는 목록에서 선택)하세요.",
                    "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "[" + isbn + "] 도서를 삭제할까요?",
                "삭제 확인", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        if (svc.deleteBook(isbn)) {
            JOptionPane.showMessageDialog(this, "도서가 삭제되었습니다.");
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "삭제 실패: 도서가 없거나 대출 중인 도서입니다.",
                    "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCollect() {
        String keyword = apiKeywordField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "수집할 키워드를 입력하세요.",
                    "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int count = svc.collectAndRegisterFromAPI(keyword);
        JOptionPane.showMessageDialog(this,
                "'" + keyword + "' 수집 결과: 신규 " + count + "권 등록됨.\n(0권이면 .env의 NL_API_KEY 설정을 확인하세요.)");
        refreshTable();
    }

    private Book readForm() {
        String isbn     = isbnField.getText().trim();
        String title    = titleField.getText().trim();
        String author   = authorField.getText().trim();
        String category = categoryField.getText().trim();
        if (isbn.isEmpty() || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ISBN과 제목은 필수입니다.",
                    "입력 오류", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return new Book(isbn, title, author, category, null);
    }

    private void clearForm() {
        isbnField.setText("");
        titleField.setText("");
        authorField.setText("");
        categoryField.setText("");
        table.clearSelection();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Book> books = svc.getAllRegisteredBooks();
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getIsbn(), b.getTitle(), b.getAuthor(), b.getCategory(), b.getStatus()});
        }
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
