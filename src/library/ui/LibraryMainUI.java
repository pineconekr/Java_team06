package library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import library.service.BookAdminSvc;
import library.service.BorrowSvc;

public class LibraryMainUI extends JFrame {

    private BorrowSvc borrowSvc;
    private BookAdminSvc bookAdminSvc;
    private boolean isAdminLoggedIn = false;  // 🔒 관리자 로그인 상태
    private boolean isStudentLoggedIn = false; // 🔓 학생 로그인 상태

    // 멤버 변수 선언
    private JButton btnCreate;
    private JButton btnUpdate;
    private JButton btnStudentLogin; // 1. 학생 로그인 버튼
    private JButton btnAdminLogin;   // 2. 관리자 로그인 버튼
    private JButton btnRegister;     // 3. 회원가입 버튼

    public LibraryMainUI(BorrowSvc borrowSvc, BookAdminSvc bookAdminSvc) {
        this.borrowSvc = borrowSvc;
        this.bookAdminSvc = bookAdminSvc;

        setTitle("도서관 관리 시스템 v2.0");

        setTitle("도서관 관리 시스템 v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600); // 3개 버튼 배치를 위해 가로폭 1000 유지
        setLocationRelativeTo(null);

        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.setBackground(new Color(245, 247, 250));

        // =================================================================
        // 1. 상단 탑바 영역 (네이비 그라데이션 + 로그인 3종 세트)
        // =================================================================
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 37, 61), 0, getHeight(), new Color(28, 50, 74));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topBar.setBorder(BorderFactory.createEmptyBorder(18, 30, 18, 30));

        JLabel mainTitle = new JLabel("도서관 관리 시스템", JLabel.LEFT);
        mainTitle.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        mainTitle.setForeground(Color.WHITE);
        topBar.add(mainTitle, BorderLayout.WEST);

        // 우측 버튼 3개를 담을 패널
        JPanel memberMenuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        memberMenuPanel.setOpaque(false);

        // [버튼 1] 학생 로그인
        btnStudentLogin = new JButton("학생 로그인");
        btnStudentLogin.setBackground(Color.WHITE);
        btnStudentLogin.setForeground(new Color(15, 37, 61));
        btnStudentLogin.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnStudentLogin.setFocusPainted(false);
        btnStudentLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // [버튼 2] 관리자 로그인 (요청하신 명칭으로 수정 완료)
        btnAdminLogin = new JButton("관리자 로그인");
        btnAdminLogin.setBackground(new Color(225, 230, 235));
        btnAdminLogin.setForeground(new Color(15, 37, 61));
        btnAdminLogin.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnAdminLogin.setFocusPainted(false);
        btnAdminLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // [버튼 3] 회원가입
        btnRegister = new JButton("회원가입");
        btnRegister.setBackground(new Color(12, 45, 74));
        btnRegister.setForeground(Color.CYAN);
        btnRegister.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 패널에 버튼 3개 장착
        memberMenuPanel.add(btnStudentLogin);
        memberMenuPanel.add(btnAdminLogin);
        memberMenuPanel.add(btnRegister);
        topBar.add(memberMenuPanel, BorderLayout.EAST);
        c.add(topBar, BorderLayout.NORTH);

        // =================================================================
        // 2. 중앙 메인 버튼 영역 (입체 카드 스타일)
        // =================================================================
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JPanel row1 = new JPanel(new GridLayout(1, 3, 25, 0));
        row1.setOpaque(false);

        JButton btnBorrow = createCoolButton("📖", "도서 대출", new Color(33, 102, 224));
        JButton btnReturn = createCoolButton("↩", "도서 반납", new Color(0, 147, 171));
        JButton btnSearch = createCoolButton("🔍", "도서 검색", new Color(123, 31, 230));

        row1.add(btnBorrow);
        row1.add(btnReturn);
        row1.add(btnSearch);

        JPanel row2Wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 25));
        row2Wrapper.setOpaque(false);

        JPanel row2 = new JPanel(new GridLayout(1, 2, 25, 0));
        row2.setPreferredSize(new Dimension(580, 150));
        row2.setOpaque(false);

        btnCreate = createCoolButton("➕", "도서 등록 (사서)", new Color(15, 157, 88));
        btnUpdate = createCoolButton("📝", "도서 정보 수정/삭제 (사서)", new Color(214, 115, 0));

        row2.add(btnCreate);
        row2.add(btnUpdate);
        row2Wrapper.add(row2);

        mainContentPanel.add(row1);
        mainContentPanel.add(row2Wrapper);
        c.add(mainContentPanel, BorderLayout.CENTER);

        updateAuthorityUI();

        // =================================================================
        // 3. 컴포넌트 이벤트 리스너 액션 정의
        // =================================================================

        // [학생 로그인] 버튼 액션
        btnStudentLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isStudentLoggedIn) {
                    String studentId = JOptionPane.showInputDialog(null, "학번(ID)을 입력하세요", "학생 로그인", JOptionPane.QUESTION_MESSAGE);
                    if (studentId != null && !studentId.trim().isEmpty()) {
                        isStudentLoggedIn = true;
                        JOptionPane.showMessageDialog(null, "🔓 학생 인증 성공: [" + studentId + "] 님 환영합니다.");
                        btnStudentLogin.setText("학생 로그아웃");
                    }
                } else {
                    isStudentLoggedIn = false;
                    JOptionPane.showMessageDialog(null, "🔒 로그아웃 되었습니다.");
                    btnStudentLogin.setText("학생 로그인");
                }
            }
        });

        // [관리자 로그인] 버튼 액션
        btnAdminLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isAdminLoggedIn) {
                    String adminId = JOptionPane.showInputDialog(null, "관리자 ID를 입력하세요 (테스트: admin)", "관리자 로그인", JOptionPane.QUESTION_MESSAGE);
                    if ("admin".equals(adminId)) {
                        isAdminLoggedIn = true;
                        JOptionPane.showMessageDialog(null, "🔓 관리자 권한이 활성화되었습니다. 하단의 [사서 전용] 메뉴가 열립니다.");
                    } else if (adminId != null) {
                        JOptionPane.showMessageDialog(null, "❌ 잘못된 관리자 ID입니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    isAdminLoggedIn = false;
                    JOptionPane.showMessageDialog(null, "🔒 관리자 기능이 잠깁니다.");
                }
                updateAuthorityUI();
            }
        });

        // [회원가입] 버튼 액션
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "신규 회원가입 창을 구동합니다.", "회원가입", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // [도서 대출] 버튼 연동
        btnBorrow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new BorrowUI(borrowSvc);
            }
        });

        // 기능 미구현 뼈대 리스너
        ActionListener dummyListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton srcBtn = (JButton) e.getSource();
                String text = srcBtn.getText().replaceAll("<[^>]*>", "").replaceAll("[0-9⭐\uD83D\uDCD6\u21A9\uD83D\uDD0D➕\uD83D\uDCDD]", "").trim();
                JOptionPane.showMessageDialog(null, "[" + text + "] 기능은 팀원이 구현 중입니다.");
            }
        };

        // [도서 반납] 버튼 연동
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame returnFrame = new JFrame("도서 반납 시스템");
                returnFrame.setSize(550, 400);
                returnFrame.setLocationRelativeTo(null);
                returnFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                returnFrame.add(new ReturnUI());
                returnFrame.setVisible(true);
            }
        });

        // [도서 검색] 버튼 연동
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame searchFrame = new JFrame("도서 검색 시스템");
                searchFrame.setSize(650, 450);
                searchFrame.setLocationRelativeTo(null);
                searchFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                searchFrame.add(new SearchUI());
                searchFrame.setVisible(true);
            }
        });

        // [도서 등록 (사서)] 버튼 연동
        btnCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 새로운 창(프레임) 만들기
                JFrame adminFrame = new JFrame("사서 전용 - 도서 관리 시스템");
                adminFrame.setSize(600, 500); // 폼과 테이블이 다 들어가야 하니 넉넉하게!
                adminFrame.setLocationRelativeTo(null); // 가운데 띄우기
                adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 이 창만 닫히게!

                // BookAdminUI 화면(패널)을 넣고, bookAdminSvc 서비스 넘겨주기
                adminFrame.add(new BookAdminUI(bookAdminSvc));

                adminFrame.setVisible(true); // 화면 보여주기!
            }
        });

        // [도서 정보 수정/삭제 (사서)] 버튼 연동
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame adminFrame = new JFrame("사서 전용 - 도서 관리 시스템");
                adminFrame.setSize(600, 500);
                adminFrame.setLocationRelativeTo(null);
                adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                // 똑같이 관리자 UI 띄워주기
                adminFrame.add(new BookAdminUI(bookAdminSvc));

                adminFrame.setVisible(true);
            }
        });

        setVisible(true);
    }

    /**
     * 관리자 로그인 상태에 따른 하단 사서 전용 버튼 제어
     */
    private void updateAuthorityUI() {
        if (isAdminLoggedIn) {
            btnAdminLogin.setText("관리자 로그아웃");
            btnCreate.setEnabled(true);
            btnUpdate.setEnabled(true);
            btnCreate.setToolTipText("도서 등록이 가능합니다.");
            btnUpdate.setToolTipText("도서 수정 및 삭제가 가능합니다.");
        } else {
            btnAdminLogin.setText("관리자 로그인");
            btnCreate.setEnabled(false);
            btnUpdate.setEnabled(false);
            btnCreate.setToolTipText("관리자 로그인 후 사용 가능한 기능입니다.");
            btnUpdate.setToolTipText("관리자 로그인 후 사용 가능한 기능입니다.");
        }
    }

    /**
     * 버튼 스타일 팩토리 메서드
     */
    private JButton createCoolButton(String icon, String title, Color pointColor) {
        String htmlText = "<html><center><font size='6' color='" + String.format("#%02x%02x%02x", pointColor.getRed(), pointColor.getGreen(), pointColor.getBlue()) + "'>" + icon + "</font><br><br>"
                + "<font size='4' color='#2C3E50'><b>" + title + "</b></font></center></html>";

        JButton button = new JButton(htmlText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isEnabled()) {
                    g2.setColor(new Color(230, 234, 238));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(250, 252, 255));
                } else {
                    g2.setColor(Color.WHITE);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                if (isEnabled()) {
                    g2.setColor(pointColor);
                } else {
                    g2.setColor(Color.LIGHT_GRAY);
                }
                g2.fillRoundRect(0, 0, getWidth(), 10, 16, 16);
                g2.fillRect(0, 5, getWidth(), 5);

                g2.setColor(new Color(218, 224, 233));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
}