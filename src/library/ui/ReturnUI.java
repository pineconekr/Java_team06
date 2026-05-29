package library.ui;

import library.model.Loan;
import library.service.HistorySvc;
import library.service.ReturnSvc;
import library.service.ReturnSvc.ReturnInfo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// 도서 반납 화면 (회원 중심: 회원 조회 -> 대출 목록 -> 선택/전체 반납)
public class ReturnUI extends JPanel {

    private final ReturnSvc svc;
    private final HistorySvc historySvc;

    private final JTextField memberIdField = new JTextField(12);
    private final JTextField isbnField     = new JTextField(16);
    private final JTextArea  resultArea    = new JTextArea(6, 40);

    private String currentMemberId = null; // 마지막으로 조회한 회원(반납 후 새로고침용)

    private final String[] COLUMNS = {"대출ID", "제목", "ISBN", "반납기한", "상태"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public ReturnUI(ReturnSvc svc, HistorySvc historySvc) {
        this.svc = svc;
        this.historySvc = historySvc;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildResultPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildTopPanel() {
        JPanel wrap = new JPanel(new GridLayout(2, 1, 0, 6));

        // 회원 조회
        JPanel lookup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        lookup.setBorder(new TitledBorder("회원으로 반납"));
        lookup.add(new JLabel("회원 ID:"));
        lookup.add(memberIdField);
        JButton lookupBtn = new JButton("대출 목록 조회");
        lookupBtn.addActionListener(e -> loadMemberLoans());
        memberIdField.addActionListener(e -> loadMemberLoans());
        lookup.add(lookupBtn);

        // ISBN 직접 반납(바코드 스캔용 보조 경로)
        JPanel byIsbn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        byIsbn.setBorder(new TitledBorder("ISBN 직접 반납 (바코드)"));
        byIsbn.add(new JLabel("ISBN:"));
        byIsbn.add(isbnField);
        JButton isbnBtn = new JButton("반납");
        isbnBtn.addActionListener(e -> handleByIsbn());
        isbnField.addActionListener(e -> handleByIsbn());
        byIsbn.add(isbnBtn);

        wrap.add(lookup);
        wrap.add(byIsbn);
        return wrap;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(new TitledBorder("대출 중 목록"));
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton selectedBtn = new JButton("선택 반납");
        JButton allBtn = new JButton("전체 반납");
        selectedBtn.addActionListener(e -> returnSelected());
        allBtn.addActionListener(e -> returnAll());
        btns.add(selectedBtn);
        btns.add(allBtn);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("처리 결과"));
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultArea.setLineWrap(true);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return panel;
    }

    /** 회원의 대출 중 목록을 표에 채운다. */
    private void loadMemberLoans() {
        String memberId = memberIdField.getText().trim();
        if (memberId.isEmpty()) { showError("회원 ID를 입력하세요."); return; }

        currentMemberId = memberId;
        tableModel.setRowCount(0);
        List<Loan> active = historySvc.getActive(memberId);
        for (Loan l : active) {
            String status = l.isOverdue() ? ("연체 " + l.getOverdueDays() + "일") : "대출중";
            tableModel.addRow(new Object[]{
                    l.getLoanId(), historySvc.getBookTitle(l), l.getIsbn(), l.getDueDate(), status});
        }

        if (active.isEmpty()) {
            resultArea.setText("[" + memberId + "] 대출 중인 도서가 없습니다.\n(존재하지 않는 회원이거나 모두 반납됨)");
        } else {
            resultArea.setText("[" + memberId + "] 대출 중 " + active.size() + "권. 반납할 항목을 선택하거나 [전체 반납]을 누르세요.");
        }
    }

    private void returnSelected() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) { showError("반납할 항목을 목록에서 선택하세요."); return; }
        List<Integer> ids = new ArrayList<>();
        for (int r : rows) ids.add((Integer) tableModel.getValueAt(r, 0));
        processReturns(ids);
    }

    private void returnAll() {
        if (tableModel.getRowCount() == 0) { showError("반납할 대출 목록이 없습니다. 먼저 회원을 조회하세요."); return; }
        List<Integer> ids = new ArrayList<>();
        for (int r = 0; r < tableModel.getRowCount(); r++) ids.add((Integer) tableModel.getValueAt(r, 0));
        processReturns(ids);
    }

    /** 여러 건을 반납하고 결과(성공/연체 패널티)를 요약한다. */
    private void processReturns(List<Integer> loanIds) {
        StringBuilder sb = new StringBuilder();
        int success = 0, penalty = 0;
        for (int id : loanIds) {
            ReturnInfo info = svc.returnBook(id);
            switch (info.result) {
                case SUCCESS -> {
                    success++;
                    sb.append("[완료] 대출 ").append(id).append(" 반납 (").append(info.loan.getIsbn()).append(")\n");
                }
                case SUCCESS_PENALTY -> {
                    success++; penalty++;
                    sb.append("[완료] 대출 ").append(id).append(" 반납 - 연체 ")
                      .append(info.overdueDays).append("일 -> ").append(info.penaltyDays)
                      .append("일 정지 (해제일 ").append(info.suspendedUntil).append(")\n");
                }
                case ALREADY_RETURNED -> sb.append("[건너뜀] 대출 ").append(id).append(" 이미 반납됨\n");
                case LOAN_NOT_FOUND   -> sb.append("[실패] 대출 ").append(id).append(" 기록 없음\n");
            }
        }
        resultArea.setText(sb.toString());

        String summary = "반납 완료: " + success + "건"
                + (penalty > 0 ? " (연체 패널티 " + penalty + "건 적용)" : "");
        JOptionPane.showMessageDialog(this, summary, "반납 결과",
                penalty > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);

        if (currentMemberId != null) loadMemberLoans(); // 목록 새로고침
    }

    /** ISBN 직접 반납(바코드). 회원 목록과 무관하게 동작. */
    private void handleByIsbn() {
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) { showError("ISBN을 입력하세요."); return; }

        ReturnInfo info = svc.returnByIsbn(isbn);
        switch (info.result) {
            case SUCCESS -> resultArea.setText("[완료] " + isbn + " 반납 완료\n반납일: " + info.loan.getReturnDate());
            case SUCCESS_PENALTY -> {
                resultArea.setText("[완료] " + isbn + " 반납 (연체 패널티)\n"
                        + "연체 " + info.overdueDays + "일 -> " + info.penaltyDays + "일 정지\n"
                        + "정지 해제일: " + info.suspendedUntil);
                JOptionPane.showMessageDialog(this,
                        "연체 " + info.overdueDays + "일 -> " + info.penaltyDays + "일 대출 정지\n해제일: " + info.suspendedUntil,
                        "연체 패널티", JOptionPane.WARNING_MESSAGE);
            }
            case LOAN_NOT_FOUND   -> showError("해당 ISBN으로 대출 중인 기록이 없습니다.");
            case ALREADY_RETURNED -> showError("이미 반납된 도서입니다.");
        }
        isbnField.setText("");
        if (currentMemberId != null) loadMemberLoans();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "입력 오류", JOptionPane.ERROR_MESSAGE);
    }
}
