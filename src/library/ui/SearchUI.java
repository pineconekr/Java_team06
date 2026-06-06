package library.ui;

import library.model.Book;
import library.model.BookStatus;
import library.model.BorrowResult;
import library.service.BookAdminSvc;
import library.service.BorrowSvc;
import library.service.SearchSvc;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;

// 도서 검색 + 대출 + 표지 이미지 통합 화면
public class SearchUI extends JPanel {

    private final SearchSvc svc;
    private final BookAdminSvc adminSvc;
    private final BorrowSvc borrowSvc;

    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> typeBox = new JComboBox<>(
            new String[]{"전체", "제목", "저자", "ISBN", "카테고리"});
    private final JButton searchBtn = new JButton("검색");
    private final JLabel statusLabel = new JLabel(" ");

    private final JTextField tfBorrowMemberId = new JTextField(10);

    private final String[] COLUMNS = {"ISBN", "제목", "저자", "카테고리", "상태"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    // 🌟 책 표지를 보여줄 액자(Label)와, 현재 표에 띄워진 책 목록을 기억할 리스트
    private final JLabel coverLabel = new JLabel("책을 선택하세요", SwingConstants.CENTER);
    private List<Book> currentResults;

    public SearchUI(SearchSvc svc, BookAdminSvc adminSvc, BorrowSvc borrowSvc, String loggedInId, boolean isAdmin) {
        this.svc = svc;
        this.adminSvc = adminSvc;
        this.borrowSvc = borrowSvc;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 상단 검색 바
        add(buildTopPanel(), BorderLayout.NORTH);

        // 🌟 중앙 영역 (왼쪽: 도서 목록 표 / 오른쪽: 책 표지 액자)
        JPanel centerWrap = new JPanel(new BorderLayout(10, 0));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        table.setRowHeight(25);
        centerWrap.add(new JScrollPane(table), BorderLayout.CENTER);

        // 표지 액자 패널 설정
        JPanel coverPanel = new JPanel(new BorderLayout());
        coverPanel.setPreferredSize(new Dimension(160, 0));
        coverPanel.setBorder(new TitledBorder("도서 표지"));
        coverLabel.setPreferredSize(new Dimension(140, 200));
        coverPanel.add(coverLabel, BorderLayout.NORTH); // 위쪽에 달라붙게 배치
        centerWrap.add(coverPanel, BorderLayout.EAST);

        add(centerWrap, BorderLayout.CENTER);

        // 하단 대출 버튼 영역
        add(buildBottomPanel(loggedInId, isAdmin), BorderLayout.SOUTH);

        // 🌟 [핵심] 표에서 책을 클릭했을 때 이벤트
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 클릭한 줄의 책 정보 가져오기
                int row = table.getSelectedRow();
                if (row >= 0 && currentResults != null && row < currentResults.size()) {
                    Book selectedBook = currentResults.get(row);

                    // (예: getCover_url(), getImageUrl() 등 에러 시 이름 확인 필요)
                    String coverUrl = selectedBook.getCoverUrl();

                    updateCoverImage(coverUrl); // 표지 업데이트 로직 실행
                }

                // 더블클릭 시 바로 대출
                if (e.getClickCount() == 2) {
                    processBorrow();
                }
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

    private JPanel buildBottomPanel(String loggedInId, boolean isAdmin) {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        statusLabel.setForeground(new Color(33, 102, 224));
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        JPanel borrowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        borrowPanel.add(new JLabel("대출 회원 ID:"));

        if (loggedInId != null && !isAdmin) {
            tfBorrowMemberId.setText(loggedInId);
            tfBorrowMemberId.setEditable(false);
            tfBorrowMemberId.setBackground(new Color(240, 240, 240));
        }
        borrowPanel.add(tfBorrowMemberId);

        JButton btnBorrow = new JButton("선택 도서 대출");
        btnBorrow.setBackground(new Color(33, 102, 224));
        btnBorrow.setForeground(Color.black);
        btnBorrow.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnBorrow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBorrow.addActionListener(e -> processBorrow());
        borrowPanel.add(btnBorrow);

        bottomPanel.add(borrowPanel, BorderLayout.EAST);
        return bottomPanel;
    }

    private void search() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            showResults(svc.searchAll(""));
            statusLabel.setText("전체 도서 목록 조회됨");
            return;
        }

        String type = (String) typeBox.getSelectedItem();
        List<Book> result = localSearch(type, keyword);

        if (!result.isEmpty()) {
            showResults(result);
            statusLabel.setText("로컬 도서관에서 " + result.size() + "건 찾음");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "로컬 도서관에 결과가 없습니다.\n국립중앙도서관에서 검색해 가져올까요?",
                "온라인 검색", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            showResults(result);
            statusLabel.setText("결과 없음");
            return;
        }
        fetchFromApi(keyword);
    }

    private void processBorrow() {
        String memberId = tfBorrowMemberId.getText().trim();
        if (memberId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "대출할 회원 ID를 입력해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "목록에서 대출할 도서를 선택해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String isbn = (String) tableModel.getValueAt(row, 0);
        String status = tableModel.getValueAt(row, 4).toString();

        if (!status.equals(BookStatus.AVAILABLE.name())) {
            JOptionPane.showMessageDialog(this, "이미 대출 중이거나 예약된 도서입니다.", "대출 불가", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BorrowResult result = borrowSvc.borrow(memberId, isbn);

        switch (result) {
            case SUCCESS -> {
                JOptionPane.showMessageDialog(this, "대출이 성공적으로 완료되었습니다!", "성공", JOptionPane.INFORMATION_MESSAGE);
                search();
            }
            case FAIL_NOT_AVAILABLE -> JOptionPane.showMessageDialog(this, "대출 불가: 존재하지 않는 도서이거나 이미 대출 중입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            case FAIL_SUSPENDED -> JOptionPane.showMessageDialog(this, "대출 불가: 연체 패널티로 인해 정지된 회원입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            case FAIL_LIMIT_EXCEEDED -> JOptionPane.showMessageDialog(this, "대출 불가: 등급별 대출 권수 한도를 초과했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fetchFromApi(String keyword) {
        searchBtn.setEnabled(false);
        statusLabel.setText("국립중앙도서관에서 가져오는 중...");

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return adminSvc.collectAndRegisterFromAPI(keyword);
            }
            @Override
            protected void done() {
                searchBtn.setEnabled(true);
                int added;
                try { added = get(); } catch (Exception ex) {
                    statusLabel.setText("온라인 검색 실패");
                    JOptionPane.showMessageDialog(SearchUI.this, "온라인 검색 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                List<Book> result = svc.searchAll(keyword);
                showResults(result);

                if (result.isEmpty()) {
                    statusLabel.setText("온라인에서도 결과 없음");
                    JOptionPane.showMessageDialog(SearchUI.this, "국립중앙도서관에서도 결과를 찾지 못했습니다.\n(ISBN 없는 자료는 제외됩니다. 0권이면 .env의 NL_API_KEY도 확인하세요.)");
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
        this.currentResults = result; // 🌟 현재 표시된 목록 기억
        tableModel.setRowCount(0);
        for (Book b : result) {
            tableModel.addRow(new Object[]{ b.getIsbn(), b.getTitle(), b.getAuthor(), b.getCategory(), b.getStatus() });
        }
    }

    // 🌟 URL에서 이미지를 다운로드하여 액자에 끼우는 부드러운 로직 (화면 멈춤 방지)
    private void updateCoverImage(String coverUrl) {
        if (coverUrl == null || coverUrl.trim().isEmpty()) {
            coverLabel.setIcon(null);
            coverLabel.setText("이미지 없음");
            return;
        }

        coverLabel.setIcon(null);
        coverLabel.setText("로딩 중...");

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                URL url = new URL(coverUrl);
                Image img = ImageIO.read(url);
                if (img != null) {
                    Image resized = img.getScaledInstance(140, 200, Image.SCALE_SMOOTH);
                    return new ImageIcon(resized);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        coverLabel.setText("");
                        coverLabel.setIcon(icon);
                    } else {
                        coverLabel.setText("이미지 없음");
                    }
                } catch (Exception ex) {
                    coverLabel.setText("로딩 실패");
                }
            }
        }.execute();
    }
}