package library.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// 필요한 서비스와 저장소 임포트
import library.service.BookAdminSvc;
import library.service.BorrowSvc;
import library.service.HistorySvc;
import library.service.ReturnSvc;
import library.service.SearchSvc;
import library.repository.IMemberRepository;

public class LibraryMainUI extends JFrame {

    // 🌟 팀원들이 만든 서비스 객체들을 담을 변수들
    private BorrowSvc borrowSvc;
    private SearchSvc searchSvc;
    private ReturnSvc returnSvc;
    private HistorySvc historySvc;
    private BookAdminSvc bookAdminSvc;
    private IMemberRepository memberRepo;

    private boolean isAdminLoggedIn = false;  // 🔒 관리자 로그인 상태
    private boolean isStudentLoggedIn = false; // 🔓 학생 로그인 상태
    private String loggedInStudentId = null;   // 로그인한 학생 ID(내 대출현황 조회용)

    // 멤버 변수 선언
    private JButton btnCreate;
    private JButton btnUpdate;
    private JButton btnStudentLogin;
    private JButton btnAdminLogin;
    private JButton btnRegister;
    private JButton btnMyLoans;   // 학생 전용: 내 대출현황
    private JButton btnHistory;   // 사서 전용: 대출 이력 / 연체

    // 🌟 1. 팀원들의 Main.java 규격에 맞춘 6개짜리 생성자
    public LibraryMainUI(BorrowSvc borrowSvc, SearchSvc searchSvc, ReturnSvc returnSvc,
                         HistorySvc historySvc, BookAdminSvc bookAdminSvc, IMemberRepository memberRepo) {

        // 넘어온 서비스들을 변수에 저장
        this.borrowSvc = borrowSvc;
        this.searchSvc = searchSvc;
        this.returnSvc = returnSvc;
        this.historySvc = historySvc;
        this.bookAdminSvc = bookAdminSvc;
        this.memberRepo = memberRepo;

        setTitle("도서관 관리 시스템 v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.setBackground(new Color(245, 247, 250));

        // =================================================================
        // 2. 상단 탑바 영역 (로그인 버튼 3종)
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

        JPanel memberMenuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        memberMenuPanel.setOpaque(false);

        btnStudentLogin = new JButton("학생 로그인");
        btnStudentLogin.setBackground(Color.WHITE);
        btnStudentLogin.setForeground(new Color(15, 37, 61));
        btnStudentLogin.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnStudentLogin.setFocusPainted(false);
        btnStudentLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAdminLogin = new JButton("관리자 로그인");
        btnAdminLogin.setBackground(new Color(225, 230, 235));
        btnAdminLogin.setForeground(new Color(15, 37, 61));
        btnAdminLogin.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnAdminLogin.setFocusPainted(false);
        btnAdminLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnRegister = new JButton("회원가입");
        btnRegister.setBackground(new Color(12, 45, 74));
        btnRegister.setForeground(Color.CYAN);
        btnRegister.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));

        memberMenuPanel.add(btnStudentLogin);
        memberMenuPanel.add(btnAdminLogin);
        memberMenuPanel.add(btnRegister);
        topBar.add(memberMenuPanel, BorderLayout.EAST);
        c.add(topBar, BorderLayout.NORTH);

        // =================================================================
        // 3. 중앙 메인 메뉴 영역 (아이콘 버튼 5개)
        // =================================================================
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JPanel row1 = new JPanel(new GridLayout(1, 4, 25, 0));
        row1.setOpaque(false);

        JButton btnBorrow = createCoolButton("📖", "도서 대출", new Color(33, 102, 224));
        JButton btnReturn = createCoolButton("↩", "도서 반납", new Color(0, 147, 171));
        JButton btnSearch = createCoolButton("🔍", "도서 검색", new Color(123, 31, 230));
        btnMyLoans = createCoolButton("🧾", "내 대출현황", new Color(0, 120, 140));

        row1.add(btnBorrow);
        row1.add(btnReturn);
        row1.add(btnSearch);
        row1.add(btnMyLoans);

        JPanel row2Wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 25));
        row2Wrapper.setOpaque(false);

        JPanel row2 = new JPanel(new GridLayout(1, 3, 25, 0));
        row2.setPreferredSize(new Dimension(880, 150));
        row2.setOpaque(false);

        btnCreate = createCoolButton("➕", "도서 등록 (사서)", new Color(15, 157, 88));
        btnUpdate = createCoolButton("📝", "도서 정보 수정/삭제 (사서)", new Color(214, 115, 0));
        btnHistory = createCoolButton("📋", "대출 이력 / 연체 (사서)", new Color(190, 80, 30));

        row2.add(btnCreate);
        row2.add(btnUpdate);
        row2.add(btnHistory);
        row2Wrapper.add(row2);

        mainContentPanel.add(row1);
        mainContentPanel.add(row2Wrapper);
        c.add(mainContentPanel, BorderLayout.CENTER);

        updateAuthorityUI();

        // =================================================================
        // 4. 컴포넌트 이벤트 리스너 (기능 연결)
        // =================================================================

        // [학생 로그인] 버튼 액션
        btnStudentLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isStudentLoggedIn) {
                    String studentId = JOptionPane.showInputDialog(null, "학번(ID)을 입력하세요", "학생 로그인", JOptionPane.QUESTION_MESSAGE);
                    if (studentId != null && !studentId.trim().isEmpty()) {
                        String id = studentId.trim();

                        // 🌟 memberRepo를 통해 DB에서 해당 학번의 회원 정보를 찾습니다.
                        java.util.Optional<library.model.Member> memberOpt = memberRepo.findById(id);

                        if (memberOpt.isPresent()) {
                            // 회원이 존재하면 정보를 꺼내서 이름을 가져옵니다.
                            library.model.Member member = memberOpt.get();

                            isStudentLoggedIn = true;
                            // loggedInStudentId = id; // (변수 그대로 유지)

                            // 학번 대신 member.getName()으로 이름을 출력합니다!
                            JOptionPane.showMessageDialog(null, "🔓 학생 인증 성공: [" + member.getName() + "] 님 환영합니다.");
                            btnStudentLogin.setText("학생 로그아웃");
                        } else {
                            // 🌟 DB에 없는 학번을 입력했을 경우의 예외 처리
                            JOptionPane.showMessageDialog(null, "❌ 등록되지 않은 학번입니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    isStudentLoggedIn = false;
                    // loggedInStudentId = null; // (근형님이 만드신 변수 그대로 유지)
                    JOptionPane.showMessageDialog(null, "🔒 로그아웃 되었습니다.");
                    btnStudentLogin.setText("학생 로그인");
                }
                updateAuthorityUI();
            }
        });

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
                // 🌟 새로 만든 가입 창을 띄우면서 DB 연결용 memberRepo를 넘겨줍니다!
                new RegisterUI(memberRepo);
            }
        });

        // 🌟 [도서 대출]
        btnBorrow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new BorrowUI(borrowSvc);
            }
        });

        // 🌟 [도서 반납] - returnSvc, historySvc 연결
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame returnFrame = new JFrame("도서 반납 시스템");
                returnFrame.setSize(550, 400);
                returnFrame.setLocationRelativeTo(null);
                returnFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                returnFrame.add(new ReturnUI(returnSvc, historySvc));
                returnFrame.setVisible(true);
            }
        });

        // 🌟 [도서 검색] - searchSvc, bookAdminSvc 연결
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame searchFrame = new JFrame("도서 검색 시스템");
                searchFrame.setSize(650, 450);
                searchFrame.setLocationRelativeTo(null);
                searchFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                searchFrame.add(new SearchUI(searchSvc, bookAdminSvc));
                searchFrame.setVisible(true);
            }
        });

        // 🌟 [도서 등록 (사서)]
        btnCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame adminFrame = new JFrame("사서 전용 - 도서 관리 시스템");
                adminFrame.setSize(600, 500);
                adminFrame.setLocationRelativeTo(null);
                adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                adminFrame.add(new BookAdminUI(bookAdminSvc));
                adminFrame.setVisible(true);
            }
        });

        // 🌟 [도서 수정/삭제 (사서)]
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame adminFrame = new JFrame("사서 전용 - 도서 관리 시스템");
                adminFrame.setSize(600, 500);
                adminFrame.setLocationRelativeTo(null);
                adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                adminFrame.add(new BookAdminUI(bookAdminSvc));
                adminFrame.setVisible(true);
            }
        });

        // 🌟 [내 대출현황] - 학생 본인 ID 고정, 읽기 전용
        btnMyLoans.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loggedInStudentId == null) {
                    JOptionPane.showMessageDialog(null, "학생 로그인 후 사용 가능합니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                JFrame f = new JFrame("내 대출현황 - " + loggedInStudentId);
                f.setSize(760, 480);
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                f.add(new MyLoansUI(historySvc, loggedInStudentId));
                f.setVisible(true);
            }
        });

        // 🌟 [대출 이력 / 연체] - 사서 전용(전체 회원 조회)
        btnHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame f = new JFrame("대출 이력 / 연체 조회 (사서)");
                f.setSize(780, 480);
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                f.add(new HistoryUI(historySvc));
                f.setVisible(true);
            }
        });

        setVisible(true);
    }

    private void updateAuthorityUI() {
        // 사서 전용: 등록/수정/대출이력
        boolean staff = isAdminLoggedIn;
        btnAdminLogin.setText(staff ? "관리자 로그아웃" : "관리자 로그인");
        btnCreate.setEnabled(staff);
        btnUpdate.setEnabled(staff);
        btnHistory.setEnabled(staff);
        String staffTip = staff ? null : "관리자 로그인 후 사용 가능한 기능입니다.";
        btnCreate.setToolTipText(staff ? "도서 등록이 가능합니다." : staffTip);
        btnUpdate.setToolTipText(staff ? "도서 수정 및 삭제가 가능합니다." : staffTip);
        btnHistory.setToolTipText(staff ? "전체 대출 이력/연체를 조회합니다." : staffTip);

        // 학생 전용: 내 대출현황
        btnMyLoans.setEnabled(isStudentLoggedIn);
        btnMyLoans.setToolTipText(isStudentLoggedIn
                ? "내 대출/연체 현황을 봅니다." : "학생 로그인 후 사용 가능합니다.");
    }

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