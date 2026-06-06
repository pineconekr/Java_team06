package library.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import library.service.BorrowSvc;
import library.service.SearchSvc;
import library.model.BorrowResult;
import library.model.Book;
import library.model.BookStatus;

public class BorrowUI extends JFrame {

    private BorrowSvc borrowSvc;
    private SearchSvc searchSvc; // 🌟 책 목록을 불러오기 위해 검색 서비스 추가

    private JTextField tfMemberId;
    private DefaultTableModel tableModel;
    private JTable table;

    public BorrowUI(BorrowSvc borrowSvc, SearchSvc searchSvc, String loggedInId, boolean isAdmin) {
        this.borrowSvc = borrowSvc;
        this.searchSvc = searchSvc;

        setTitle("도서 대출 시스템");
        setSize(650, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Container c = getContentPane();
        c.setLayout(new BorderLayout(10, 10));
        c.setBackground(Color.WHITE);
        ((JPanel)c).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ==========================================
        // 1. 상단: 회원 ID 입력 영역 (자동 완성)
        // ==========================================
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTop.setOpaque(false);
        panelTop.add(new JLabel("회원 ID: "));
        tfMemberId = new JTextField(15);

        // 🌟 학생 로그인 상태라면 학번을 자동으로 채우고 수정 못 하게 잠금
        if (loggedInId != null && !isAdmin) {
            tfMemberId.setText(loggedInId);
            tfMemberId.setEditable(false);
            tfMemberId.setBackground(new Color(240, 240, 240));
        }
        panelTop.add(tfMemberId);
        c.add(panelTop, BorderLayout.NORTH);

        // ==========================================
        // 2. 중앙: 도서 목록 표 영역
        // ==========================================
        String[] colNames = {"ISBN", "제목", "저자", "상태"};
        tableModel = new DefaultTableModel(colNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; } // 읽기 전용
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        table.setRowHeight(25);

        JPanel panelCenter = new JPanel(new BorderLayout());
        panelCenter.setBorder(new TitledBorder("대출 가능한 도서 목록 (마우스로 선택하세요)"));
        panelCenter.setOpaque(false);
        panelCenter.add(new JScrollPane(table), BorderLayout.CENTER);
        c.add(panelCenter, BorderLayout.CENTER);

        // ==========================================
        // 3. 하단: 대출 버튼 영역
        // ==========================================
        JButton btnSubmit = new JButton("선택한 도서 대출하기");
        btnSubmit.setBackground(new Color(33, 102, 224));
        btnSubmit.setForeground(Color.BLACK);
        btnSubmit.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBottom.setOpaque(false);
        panelBottom.add(btnSubmit);
        c.add(panelBottom, BorderLayout.SOUTH);

        // 프로그램 시작 시 도서 목록 불러오기
        loadBooks();

        // ==========================================
        // 4. 이벤트 리스너 (동작 연결)
        // ==========================================

        // 버튼 클릭 시 대출 진행
        btnSubmit.addActionListener(e -> processBorrow());

        // 표를 '더블 클릭'해도 바로 대출되게 하는 센스있는 기능!
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    processBorrow();
                }
            }
        });

        setVisible(true);
    }

    // 도서 목록을 불러와서 표에 채우는 메서드
    private void loadBooks() {
        tableModel.setRowCount(0);
        List<Book> books = searchSvc.searchAll(""); // 빈 칸으로 검색 = 전체 도서
        for (Book b : books) {
            tableModel.addRow(new Object[]{b.getIsbn(), b.getTitle(), b.getAuthor(), b.getStatus()});
        }
    }

    // 실제 대출을 처리하는 메서드
    private void processBorrow() {
        String memberId = tfMemberId.getText().trim();
        if (memberId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "회원 ID를 입력해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "목록에서 대출할 도서를 선택해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 표에서 선택한 줄의 ISBN과 상태 값을 쏙 빼옵니다.
        String isbn = (String) tableModel.getValueAt(row, 0);
        String status = tableModel.getValueAt(row, 3).toString();

        if (!status.equals(BookStatus.AVAILABLE.name())) {
            JOptionPane.showMessageDialog(this, "이미 대출 중이거나 예약된 도서입니다.", "대출 불가", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 대출 실행
        BorrowResult result = borrowSvc.borrow(memberId, isbn);

        switch (result) {
            case SUCCESS -> {
                JOptionPane.showMessageDialog(this, "대출이 성공적으로 완료되었습니다!", "성공", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // 성공하면 창 닫기
            }
            case FAIL_NOT_AVAILABLE -> JOptionPane.showMessageDialog(this, "대출 불가: 존재하지 않는 도서이거나 이미 대출 중입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            case FAIL_SUSPENDED -> JOptionPane.showMessageDialog(this, "대출 불가: 연체 패널티로 인해 정지된 회원입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            case FAIL_LIMIT_EXCEEDED -> JOptionPane.showMessageDialog(this, "대출 불가: 등급별 대출 권수 한도를 초과했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}