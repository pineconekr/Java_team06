package library.ui;

import library.model.Loan;
import library.service.HistorySvc;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

// 학생용 '내 대출현황' (로그인한 본인 ID의 대출만 조회, 읽기 전용)
// 보안: 회원 ID 입력란이 없어 남의 현황은 볼 수 없다. 반납 기능도 없다.
// 대출 목록 표시는 공용 컴포넌트 LoanTablePanel에 위임.
public class MyLoansUI extends JPanel {

    private final HistorySvc svc;
    private final String memberId;
    private final LoanTablePanel loanTable;

    private final JRadioButton activeBtn = new JRadioButton("대출 중", true);
    private final JRadioButton allBtn    = new JRadioButton("전체 이력");

    public MyLoansUI(HistorySvc svc, String memberId) {
        this.svc = svc;
        this.memberId = memberId;
        this.loanTable = new LoanTablePanel(svc);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTopPanel(), BorderLayout.NORTH);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setBorder(new TitledBorder("대출 목록"));
        tableWrap.add(loanTable, BorderLayout.CENTER);
        add(tableWrap, BorderLayout.CENTER);

        reload();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setBorder(new TitledBorder("내 대출현황"));

        JLabel who = new JLabel("회원 " + memberId + " 님");
        who.setFont(who.getFont().deriveFont(Font.BOLD));
        panel.add(who);
        panel.add(Box.createHorizontalStrut(16));

        ButtonGroup group = new ButtonGroup();
        for (JRadioButton b : new JRadioButton[]{activeBtn, allBtn}) {
            group.add(b);
            b.addActionListener(e -> reload());
            panel.add(b);
        }

        JButton refresh = new JButton("새로고침");
        refresh.addActionListener(e -> reload());
        panel.add(refresh);
        return panel;
    }

    private void reload() {
        List<Loan> loans = activeBtn.isSelected() ? svc.getActive(memberId) : svc.getHistory(memberId);
        loanTable.setLoans(loans);
    }
}
