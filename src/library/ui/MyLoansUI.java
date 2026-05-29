package library.ui;

import library.model.Loan;
import library.service.HistorySvc;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// 학생용 '내 대출현황' (로그인한 본인 ID의 대출만 조회, 읽기 전용)
// 보안: 회원 ID 입력란이 없어 남의 현황은 볼 수 없다. 반납 기능도 없다(사서 전용).
public class MyLoansUI extends JPanel {

    private final HistorySvc svc;
    private final String memberId;

    private final JRadioButton activeBtn = new JRadioButton("대출 중", true);
    private final JRadioButton allBtn    = new JRadioButton("전체 이력");

    private final String[] COLUMNS = {"대출ID", "제목", "ISBN", "대출일", "반납기한", "반납일", "상태"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public MyLoansUI(HistorySvc svc, String memberId) {
        this.svc = svc;
        this.memberId = memberId;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

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

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("대출 목록"));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void reload() {
        List<Loan> loans = activeBtn.isSelected() ? svc.getActive(memberId) : svc.getHistory(memberId);
        tableModel.setRowCount(0);
        for (Loan loan : loans) {
            tableModel.addRow(svc.toRow(loan));
        }
    }
}
