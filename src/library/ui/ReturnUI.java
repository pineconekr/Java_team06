package library.ui;

import library.service.HistorySvc;
import library.service.ReturnSvc;
import library.service.ReturnSvc.ReturnInfo;
import library.service.ReturnSvc.ReturnResult;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

// 도서 반납 화면 (사서 전용)
//  실제 도서관처럼 반납은 데스크에서 사서가 처리한다.
//   1) ISBN(바코드) 직접 반납 — 책을 스캔
//   2) 회원 조회 후 목록에서 마우스로 선택/전체 반납
//  학생은 반납하지 않는다(읽기 전용 '내 대출현황'만 사용). 피드백은 팝업으로만.
public class ReturnUI extends JPanel {

    private final ReturnSvc svc;
    private final HistorySvc historySvc;
    private final LoanTablePanel loanTable;

    private final JTextField isbnField     = new JTextField(16);
    private final JTextField memberIdField = new JTextField(12);
    private String currentMemberId = null;

    public ReturnUI(ReturnSvc svc, HistorySvc historySvc) {
        this.svc = svc;
        this.historySvc = historySvc;
        this.loanTable = new LoanTablePanel(historySvc);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
    }

    private JPanel buildTopPanel() {
        JPanel wrap = new JPanel(new GridLayout(2, 1, 0, 6));

        // 1) ISBN(바코드) 직접 반납 — 데스크 기본 흐름(책 스캔)
        JPanel byIsbn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        byIsbn.setBorder(new TitledBorder("ISBN(바코드) 반납 — 책을 스캔하세요"));
        byIsbn.add(new JLabel("ISBN:"));
        byIsbn.add(isbnField);
        JButton isbnBtn = new JButton("반납");
        isbnBtn.addActionListener(e -> handleByIsbn());
        isbnField.addActionListener(e -> handleByIsbn());
        byIsbn.add(isbnBtn);
        wrap.add(byIsbn);

        // 2) 회원 조회 후 목록에서 선택 반납 (보조)
        JPanel lookup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        lookup.setBorder(new TitledBorder("회원 조회 후 선택 반납"));
        lookup.add(new JLabel("회원 ID:"));
        lookup.add(memberIdField);
        JButton lookupBtn = new JButton("대출 목록 조회");
        lookupBtn.addActionListener(e -> loadMemberLoans());
        memberIdField.addActionListener(e -> loadMemberLoans());
        lookup.add(lookupBtn);
        wrap.add(lookup);

        return wrap;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(new TitledBorder("대출 중 목록 (마우스로 선택)"));
        panel.add(loanTable, BorderLayout.CENTER);

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

    /** 회원의 대출 중 목록을 공용 테이블에 채운다. */
    private void loadMemberLoans() {
        String memberId = memberIdField.getText().trim();
        if (memberId.isEmpty()) { showError("회원 ID를 입력하세요."); return; }
        currentMemberId = memberId;
        loanTable.setLoans(historySvc.getActive(memberId));
        if (loanTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "[" + memberId + "] 대출 중인 도서가 없습니다.",
                    "조회 결과", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void returnSelected() {
        List<Integer> ids = loanTable.getSelectedLoanIds();
        if (ids.isEmpty()) { showError("반납할 항목을 목록에서 선택하세요."); return; }
        processReturns(ids);
    }

    private void returnAll() {
        if (loanTable.getRowCount() == 0) { showError("반납할 대출 목록이 없습니다. 먼저 회원을 조회하세요."); return; }
        processReturns(loanTable.getAllLoanIds());
    }

    /** 여러 건 반납 후 결과를 팝업으로 요약. */
    private void processReturns(List<Integer> loanIds) {
        int success = 0, penalty = 0;
        for (int id : loanIds) {
            ReturnInfo info = svc.returnBook(id);
            if (info.result == ReturnResult.SUCCESS) success++;
            else if (info.result == ReturnResult.SUCCESS_PENALTY) { success++; penalty++; }
        }
        String msg = "반납 완료: " + success + "건"
                + (penalty > 0 ? "\n연체 패널티 " + penalty + "건 적용됨" : "");
        JOptionPane.showMessageDialog(this, msg, "반납 결과",
                penalty > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
        if (currentMemberId != null) loadMemberLoans(); // 목록 새로고침
    }

    /** ISBN(바코드) 직접 반납. */
    private void handleByIsbn() {
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) { showError("ISBN을 입력(스캔)하세요."); return; }

        ReturnInfo info = svc.returnByIsbn(isbn);
        switch (info.result) {
            case SUCCESS -> JOptionPane.showMessageDialog(this,
                    "반납 완료\nISBN: " + isbn + "\n반납일: " + info.loan.getReturnDate(),
                    "반납 결과", JOptionPane.INFORMATION_MESSAGE);
            case SUCCESS_PENALTY -> JOptionPane.showMessageDialog(this,
                    "반납 완료 (연체 패널티)\n연체 " + info.overdueDays + "일 -> " + info.penaltyDays
                    + "일 대출 정지\n정지 해제일: " + info.suspendedUntil,
                    "연체 패널티", JOptionPane.WARNING_MESSAGE);
            case LOAN_NOT_FOUND   -> showError("해당 ISBN으로 대출 중인 기록이 없습니다.");
            case ALREADY_RETURNED -> showError("이미 반납된 도서입니다.");
        }
        isbnField.setText("");
        if (currentMemberId != null) loadMemberLoans();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "알림", JOptionPane.ERROR_MESSAGE);
    }
}
