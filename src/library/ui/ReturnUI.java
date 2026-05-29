package library.ui;

import library.service.ReturnSvc;
import library.service.ReturnSvc.ReturnInfo;
import library.service.ReturnSvc.ReturnResult;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

// 도서 반납 화면
public class ReturnUI extends JPanel {

    private final ReturnSvc svc;

    private final JTextField loanIdField = new JTextField(12);
    private final JTextField isbnField   = new JTextField(20);
    private final JTextArea  resultArea  = new JTextArea(10, 40);

    public ReturnUI(ReturnSvc svc) {
        this.svc = svc;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(buildInputPanel(), BorderLayout.NORTH);
        add(buildResultPanel(), BorderLayout.CENTER);
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("반납 처리"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        panel.add(new JLabel("대출 ID:"), gc);
        gc.gridx = 1; panel.add(loanIdField, gc);
        JButton byId = new JButton("ID로 반납");
        byId.addActionListener(e -> handleById());
        gc.gridx = 2; panel.add(byId, gc);

        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 3; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JSeparator(), gc);
        gc.gridwidth = 1; gc.fill = GridBagConstraints.NONE;

        gc.gridx = 0; gc.gridy = 2;
        panel.add(new JLabel("ISBN:"), gc);
        gc.gridx = 1; panel.add(isbnField, gc);
        JButton byIsbn = new JButton("ISBN으로 반납");
        byIsbn.addActionListener(e -> handleByIsbn());
        gc.gridx = 2; panel.add(byIsbn, gc);

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

    private void handleById() {
        String text = loanIdField.getText().trim();
        if (text.isEmpty()) { showError("대출 ID를 입력하세요."); return; }
        try {
            displayResult(svc.returnBook(Integer.parseInt(text)));
        } catch (NumberFormatException e) {
            showError("대출 ID는 숫자여야 합니다.");
        }
    }

    private void handleByIsbn() {
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) { showError("ISBN을 입력하세요."); return; }
        displayResult(svc.returnByIsbn(isbn));
    }

    private void displayResult(ReturnInfo info) {
        StringBuilder sb = new StringBuilder();
        switch (info.result) {
            case SUCCESS -> {
                sb.append("[완료] 반납 완료\n");
                sb.append("대출 ID : ").append(info.loan.getLoanId()).append("\n");
                sb.append("ISBN    : ").append(info.loan.getIsbn()).append("\n");
                sb.append("반납일  : ").append(info.loan.getReturnDate()).append("\n");
            }
            case SUCCESS_PENALTY -> {
                sb.append("[완료] 반납 완료 (연체 패널티 적용)\n");
                sb.append("대출 ID   : ").append(info.loan.getLoanId()).append("\n");
                sb.append("ISBN      : ").append(info.loan.getIsbn()).append("\n");
                sb.append("반납일    : ").append(info.loan.getReturnDate()).append("\n");
                sb.append("───────────────────────\n");
                sb.append("연체일 수  : ").append(info.overdueDays).append("일\n");
                sb.append("대출 정지  : ").append(info.penaltyDays).append("일\n");
                sb.append("정지 해제일: ").append(info.suspendedUntil).append("\n");
                JOptionPane.showMessageDialog(this,
                        String.format("연체 %d일 -> %d일 대출 정지\n정지 해제일: %s",
                                info.overdueDays, info.penaltyDays, info.suspendedUntil),
                        "연체 패널티", JOptionPane.WARNING_MESSAGE);
            }
            case LOAN_NOT_FOUND    -> sb.append("[실패] 해당 대출 기록을 찾을 수 없습니다.\n");
            case ALREADY_RETURNED  -> sb.append("[실패] 이미 반납된 도서입니다.\n");
        }
        resultArea.setText(sb.toString());
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "입력 오류", JOptionPane.ERROR_MESSAGE);
    }
}
