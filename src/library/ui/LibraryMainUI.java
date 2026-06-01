package library.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import library.model.Member;
import library.model.MemberGrade;
import library.repository.IMemberRepository;
import library.service.BorrowSvc;
import library.service.SearchSvc;
import library.service.ReturnSvc;
import library.service.HistorySvc;
import library.service.BookAdminSvc;

public class LibraryMainUI extends JFrame {

    private BorrowSvc borrowSvc;
    private SearchSvc searchSvc;
    private ReturnSvc returnSvc;
    private HistorySvc historySvc;
    private BookAdminSvc adminSvc;
    private IMemberRepository memberRepo;

    private boolean isAdminLoggedIn = false;     // 관리자(사서) 로그인 상태
    private boolean isStudentLoggedIn = false;    // 학생 로그인 상태
    private String loggedInStudentId = null;      // 로그인한 학생의 ID(내 대출현황 조회용)

    // 권한에 따라 활성/비활성되는 버튼들
    private JButton btnStudentLogin;
    private JButton btnAdminLogin;
    private JButton btnRegister;
    private JButton btnMyLoans;   // 학생 전용
    private JButton btnReturn;    // 사서 전용
    private JButton btnHistory;   // 사서 전용
    private JButton btnCreate;    // 사서 전용
    private JButton btnUpdate;    // 사서 전용

    public LibraryMainUI(BorrowSvc borrowSvc, SearchSvc searchSvc, ReturnSvc returnSvc,
                         HistorySvc historySvc, BookAdminSvc adminSvc, IMemberRepository memberRepo) {
        this.borrowSvc  = borrowSvc;
        this.searchSvc  = searchSvc;
        this.returnSvc  = returnSvc;
        this.historySvc = historySvc;
        this.adminSvc   = adminSvc;
        this.memberRepo = memberRepo;

        setTitle("도서관 관리 시스템 v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);

        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.setBackground(new Color(245, 247, 250));

        c.add(buildTopBar(), BorderLayout.NORTH);
        c.add(buildCenter(), BorderLayout.CENTER);

        updateAuthorityUI();
        registerActions();

        setVisible(true);
    }

    // =================================================================
    // 상단 탑바 (로그인 3종)
    // =================================================================
    private JPanel buildTopBar() {
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

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        menu.setOpaque(false);

        btnStudentLogin = topButton("학생 로그인", Color.WHITE, new Color(15, 37, 61));
        btnAdminLogin   = topButton("관리자 로그인", new Color(225, 230, 235), new Color(15, 37, 61));
        btnRegister     = topButton("회원가입", new Color(12, 45, 74), Color.CYAN);

        menu.add(btnStudentLogin);
        menu.add(btnAdminLogin);
        menu.add(btnRegister);
        topBar.add(menu, BorderLayout.EAST);
        return topBar;
    }

    private JButton topButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // =================================================================
    // 중앙 메뉴 (이용자 메뉴 / 사서 전용)
    // =================================================================
    private JPanel buildCenter() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton btnSearch  = createCoolButton("도서 검색", new Color(123, 31, 230));
        JButton btnBorrow  = createCoolButton("도서 대출", new Color(33, 102, 224));
        btnMyLoans = createCoolButton("내 대출현황", new Color(0, 147, 171));

        JPanel userRow = new JPanel(new GridLayout(1, 3, 25, 0));
        userRow.setOpaque(false);
        userRow.add(btnSearch);
        userRow.add(btnBorrow);
        userRow.add(btnMyLoans);

        btnReturn  = createCoolButton("도서 반납", new Color(0, 120, 140));
        btnHistory = createCoolButton("대출 이력 / 연체", new Color(190, 80, 30));
        btnCreate  = createCoolButton("도서 등록", new Color(15, 157, 88));
        btnUpdate  = createCoolButton("도서 수정 / 삭제", new Color(214, 115, 0));

        JPanel staffRow = new JPanel(new GridLayout(1, 4, 25, 0));
        staffRow.setOpaque(false);
        staffRow.add(btnReturn);
        staffRow.add(btnHistory);
        staffRow.add(btnCreate);
        staffRow.add(btnUpdate);

        content.add(sectionLabel("이용자 메뉴"));
        content.add(fixedHeight(userRow, 150));
        content.add(Box.createVerticalStrut(20));
        content.add(sectionLabel("사서 전용 (관리자 로그인 필요)"));
        content.add(fixedHeight(staffRow, 150));

        // 항상 사용 가능한 버튼 연결
        btnSearch.addActionListener(e ->
                openFrame("도서 검색 시스템", 650, 450, new SearchUI(searchSvc, adminSvc)));
        btnBorrow.addActionListener(e -> new BorrowUI(borrowSvc));

        return content;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        l.setForeground(new Color(90, 100, 110));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 4, 6, 0));
        return l;
    }

    private JPanel fixedHeight(JPanel inner, int h) {
        inner.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        return inner;
    }

    // =================================================================
    // 이벤트 연결
    // =================================================================
    private void registerActions() {
        // [학생 로그인/로그아웃]
        btnStudentLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isStudentLoggedIn) {
                    String studentId = JOptionPane.showInputDialog(null, "학번(ID)을 입력하세요", "학생 로그인", JOptionPane.QUESTION_MESSAGE);
                    if (studentId != null && !studentId.trim().isEmpty()) {
                        isStudentLoggedIn = true;
                        loggedInStudentId = studentId.trim();
                        JOptionPane.showMessageDialog(null, "학생 인증 성공: [" + loggedInStudentId + "] 님 환영합니다.");
                    }
                } else {
                    isStudentLoggedIn = false;
                    loggedInStudentId = null;
                    JOptionPane.showMessageDialog(null, "로그아웃 되었습니다.");
                }
                updateAuthorityUI();
            }
        });

        // [관리자 로그인/로그아웃]
        btnAdminLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isAdminLoggedIn) {
                    String adminId = JOptionPane.showInputDialog(null, "관리자 ID를 입력하세요 (테스트: admin)", "관리자 로그인", JOptionPane.QUESTION_MESSAGE);
                    if ("admin".equals(adminId)) {
                        isAdminLoggedIn = true;
                        JOptionPane.showMessageDialog(null, "관리자 권한이 활성화되었습니다. [사서 전용] 메뉴가 열립니다.");
                    } else if (adminId != null) {
                        JOptionPane.showMessageDialog(null, "잘못된 관리자 ID입니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    isAdminLoggedIn = false;
                    JOptionPane.showMessageDialog(null, "관리자 기능이 잠깁니다.");
                }
                updateAuthorityUI();
            }
        });

        // [회원가입]
        btnRegister.addActionListener(e -> showRegisterDialog());

        // [내 대출현황] - 학생 본인 ID 고정, 읽기 전용
        btnMyLoans.addActionListener(e -> {
            if (loggedInStudentId == null) return;
            openFrame("내 대출현황 - " + loggedInStudentId, 760, 480,
                    new MyLoansUI(historySvc, loggedInStudentId));
        });

        // [도서 반납] - 사서 전용(전체 회원 조회/반납)
        btnReturn.addActionListener(e ->
                openFrame("도서 반납 (사서)", 640, 560, new ReturnUI(returnSvc, historySvc)));

        // [대출 이력 / 연체] - 사서 전용(전체 회원 조회/연체)
        btnHistory.addActionListener(e ->
                openFrame("대출 이력 / 연체 조회 (사서)", 780, 480, new HistoryUI(historySvc)));

        // [도서 등록], [도서 수정/삭제] - 사서 전용
        btnCreate.addActionListener(e ->
                openFrame("도서 관리 (사서)", 740, 620, new BookAdminUI(adminSvc)));
        btnUpdate.addActionListener(e ->
                openFrame("도서 관리 (사서)", 740, 620, new BookAdminUI(adminSvc)));
    }

    /** 회원가입 입력 다이얼로그 */
    private void showRegisterDialog() {
        JTextField idField   = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JComboBox<String> gradeBox = new JComboBox<>(new String[]{"일반 (REGULAR, 3권)", "프리미엄 (PREMIUM, 5권)"});

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("회원 ID :"));   panel.add(idField);
        panel.add(new JLabel("이름 :"));      panel.add(nameField);
        panel.add(new JLabel("등급 :"));      panel.add(gradeBox);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "신규 회원가입",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String id   = idField.getText().trim();
        String name = nameField.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "회원 ID와 이름은 필수입니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (memberRepo.findById(id).isPresent()) {
            JOptionPane.showMessageDialog(this, "이미 존재하는 회원 ID입니다.", "중복 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Member member = new Member(id, name);
        member.setGrade(gradeBox.getSelectedIndex() == 1 ? MemberGrade.PREMIUM : MemberGrade.REGULAR);
        memberRepo.add(member);

        JOptionPane.showMessageDialog(this,
                "회원가입 완료!\nID: " + id + " / 이름: " + name
                        + " / 등급: " + member.getGrade(),
                "가입 완료", JOptionPane.INFORMATION_MESSAGE);
    }

    /** 자식 화면(JPanel)을 별도 창으로 띄우는 공통 헬퍼. */
    private void openFrame(String title, int width, int height, JComponent content) {
        JFrame frame = new JFrame(title);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(this);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(content);
        frame.setVisible(true);
    }

    /** 로그인 상태에 따라 메뉴 권한을 갱신한다. */
    private void updateAuthorityUI() {
        btnStudentLogin.setText(isStudentLoggedIn ? "학생 로그아웃" : "학생 로그인");
        btnAdminLogin.setText(isAdminLoggedIn ? "관리자 로그아웃" : "관리자 로그인");

        // 학생 전용
        btnMyLoans.setEnabled(isStudentLoggedIn);
        btnMyLoans.setToolTipText(isStudentLoggedIn
                ? "내 대출/연체 현황을 봅니다." : "학생 로그인 후 사용 가능합니다.");

        // 사서 전용
        String staffTip = isAdminLoggedIn ? null : "관리자 로그인 후 사용 가능한 기능입니다.";
        for (JButton b : new JButton[]{btnReturn, btnHistory, btnCreate, btnUpdate}) {
            b.setEnabled(isAdminLoggedIn);
            b.setToolTipText(staffTip);
        }
    }

    /** 버튼 스타일 팩토리 메서드 (텍스트 전용 카드 버튼) */
    private JButton createCoolButton(String title, Color pointColor) {
        String htmlText = "<html><center><font size='5' color='#2C3E50'><b>" + title + "</b></font></center></html>";

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