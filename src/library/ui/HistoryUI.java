package library.ui;

import library.model.Loan;
import library.service.HistorySvc;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

// 대출 이력 / 연체 조회 화면 (사서).
// 대출 목록 표시는 공용 컴포넌트 LoanTablePanel에 위임(MyLoansUI/ReturnUI와 공유).
public class HistoryUI extends JPanel {

    private final HistorySvc svc;
    private final LoanTablePanel loanTable;

    private final JTextField memberIdField = new JTextField(15);
    private final JRadioButton allBtn      = new JRadioButton("전체", true);
    private final JRadioButton activeBtn   = new JRadioButton("대출 중");
    private final JRadioButton overdueBtn  = new JRadioButton("연체 중");

    public HistoryUI(HistorySvc svc) {
        this.svc = svc;
        this.loanTable = new LoanTablePanel(svc);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(buildSearchPanel(), BorderLayout.NORTH);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setBorder(new TitledBorder("대출 이력"));
        tableWrap.add(loanTable, BorderLayout.CENTER);
        add(tableWrap, BorderLayout.CENTER);
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setBorder(new TitledBorder("이력 조회"));

        panel.add(new JLabel("회원 ID:"));
        panel.add(memberIdField);

        ButtonGroup group = new ButtonGroup();
        for (JRadioButton btn : new JRadioButton[]{allBtn, activeBtn, overdueBtn}) {
            group.add(btn);
            panel.add(btn);
        }

        JButton searchBtn = new JButton("조회");
        searchBtn.addActionListener(e -> search());
        panel.add(searchBtn);
        return panel;
    }

    private void search() {
        String memberId = memberIdField.getText().trim();

        List<Loan> loans;
        if (overdueBtn.isSelected()) {
            loans = svc.getOverdue(); // 연체 전체 조회는 회원 ID가 필요 없다
        } else {
            if (memberId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "회원 ID를 입력하세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loans = activeBtn.isSelected() ? svc.getActive(memberId) : svc.getHistory(memberId);
        }

        loanTable.setLoans(loans);
        if (loans.isEmpty()) {
            JOptionPane.showMessageDialog(this, "조회된 이력이 없습니다.", "결과 없음", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
