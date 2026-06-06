package library.ui;

import library.model.Book;
import library.service.BookAdminSvc;
import library.service.SearchSvc;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// 도서 검색 화면 (로컬 우선 + 결과 없으면 국립중앙도서관 API 라이브 폴백 = cache-aside)
public class SearchUI extends JPanel {

    private final SearchSvc svc;
    private final BookAdminSvc adminSvc;

    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> typeBox = new JComboBox<>(
            new String[]{"전체", "제목", "저자", "ISBN", "카테고리"});
    private final JButton searchBtn = new JButton("검색");
    private final JLabel statusLabel = new JLabel(" ");

    private final String[] COLUMNS = {"ISBN", "제목", "저자", "카테고리", "상태"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel coverLabel = new JLabel();

    public SearchUI(SearchSvc svc, BookAdminSvc adminSvc) {
        this.svc = svc;
        this.adminSvc = adminSvc;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(buildTopPanel(), BorderLayout.NORTH);
        coverLabel.setHorizontalAlignment(JLabel.CENTER);
        coverLabel.setPreferredSize(new Dimension(180, 250));
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        centerPanel.add(coverLabel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);
        statusLabel.setForeground(new Color(33, 102, 224));
        add(statusLabel, BorderLayout.SOUTH);
        table.getSelectionModel() .addListSelectionListener(e -> {

         if (!e.getValueIsAdjusting()) {
             showSelectedCover();
         }

     });
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.add(new JLabel("검색어:"));
        panel.add(searchField);
        panel.add(typeBox);
        searchBtn.addActionListener(e -> search());
        searchField.addActionListener(e -> search());
        panel.add(searchBtn);
        return panel;
    }

    private void search() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;

        String type = (String) typeBox.getSelectedItem();
        List<Book> result = localSearch(type, keyword);

        if (!result.isEmpty()) {
            showResults(result);
            statusLabel.setText("로컬 도서관에서 " + result.size() + "건 찾음");
            return;
        }

        // 로컬 0건 -> 국립중앙도서관 라이브 검색 제안
        int ok = JOptionPane.showConfirmDialog(this,
                "로컬 도서관에 결과가 없습니다.\n국립중앙도서관에서 검색해 가져올까요?",
                "온라인 검색", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            showResults(result); // 빈 표
            statusLabel.setText("결과 없음");
            return;
        }
        fetchFromApi(keyword);
    }

    /** API 호출은 느리므로 SwingWorker 백그라운드에서 처리(화면 멈춤 방지). */
    private void fetchFromApi(String keyword) {
        searchBtn.setEnabled(false);
        statusLabel.setText("국립중앙도서관에서 가져오는 중...");

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                // 수집 + DB 적재(이미 있으면 건너뜀). 다음 검색부터는 로컬에서 바로 잡힘.
                return adminSvc.collectAndRegisterFromAPI(keyword);
            }

            @Override
            protected void done() {
                searchBtn.setEnabled(true);
                int added;
                try {
                    added = get();
                } catch (Exception ex) {
                    statusLabel.setText("온라인 검색 실패");
                    JOptionPane.showMessageDialog(SearchUI.this,
                            "온라인 검색 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 적재된 도서를 로컬에서 다시 조회해 표시(통합 검색 기준)
                List<Book> result = svc.searchAll(keyword);
                showResults(result);

                if (result.isEmpty()) {
                    statusLabel.setText("온라인에서도 결과 없음");
                    JOptionPane.showMessageDialog(SearchUI.this,
                            "국립중앙도서관에서도 결과를 찾지 못했습니다.\n"
                            + "(ISBN 없는 자료는 제외됩니다. 0권이면 .env의 NL_API_KEY도 확인하세요.)");
                } else {
                    statusLabel.setText("국립중앙도서관에서 신규 " + added + "권 수집 -> 총 " + result.size() + "건 표시");
                }
            }
        }.execute();
    }

    private List<Book> localSearch(String type, String keyword) {
        return switch (type) {
            case "제목"     -> svc.searchByTitle(keyword);
            case "저자"     -> svc.searchByAuthor(keyword);
            case "ISBN"    -> svc.searchByIsbn(keyword);
            case "카테고리" -> svc.searchByCategory(keyword);
            default        -> svc.searchAll(keyword);
        };
    }

    private void showResults(List<Book> result) {
        tableModel.setRowCount(0);
        for (Book b : result) {
            tableModel.addRow(new Object[]{
                    b.getIsbn(), b.getTitle(), b.getAuthor(),
                    b.getCategory(), b.getStatus()});
        }
    }

    private void showSelectedCover() {

    int row = table.getSelectedRow();

    System.out.println("showSelectedCover 호출");

    if (row < 0){
        System.out.println("선택된 행 없음");
        return;
    }

    String isbn =
            table.getValueAt(row, 0)
                 .toString();

    System.out.println("ISBN = " + isbn);

    svc.searchByIsbn(isbn)
       .stream()
       .findFirst()
       .ifPresent(book -> {
         System.out.println(
               "coverUrl = "
               + book.getCoverUrl());

           try {

               if (book.getCoverUrl() == null ||
                   book.getCoverUrl().isBlank()) {

                   coverLabel.setIcon(null);
                   return;
               }

               ImageIcon icon =
                       new ImageIcon(
                               new java.net.URL(
                                       book.getCoverUrl()
                               )
                       );

               Image image =
                       icon.getImage()
                           .getScaledInstance(
                                   160,
                                   220,
                                   Image.SCALE_SMOOTH
                           );

               coverLabel.setIcon(
                       new ImageIcon(image)
               );

           } catch (Exception ex) {

               coverLabel.setIcon(null);

           }

       });
}
}


