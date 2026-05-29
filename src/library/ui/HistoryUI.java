package library.ui;

import library.model.Loan;
import library.service.HistorySvc;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// 대출 이력 조회 화면
public class HistoryUI extends JPanel {

    private final HistorySvc svc;

    private final JTextField memberIdField = new JTextField(15);
    private final JRadioButton allBtn      = new JRadioButton("전체", true);
    private final JRadioButton activeBtn   = new JRadioButton("대출 중");
    private final JRadioButton overdueBtn  = new JRadioButton("연체 중");

    private final String[] COLUMNS = {"대출ID", "제목", "ISBN", "대출일", "반납기한", "반납일", "상태"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public HistoryUI(HistorySvc svc) {
        this.svc = svc;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(buildSearchPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
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

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("대출 이력"));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void search() {
        String memberId = memberIdField.getText().trim();

        List<Loan> loans;
        if (overdueBtn.isSelected()) {
            // 연체 전체 조회는 회원 ID가 필요 없다.
            loans = svc.getOverdue();
        } else {
            if (memberId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "회원 ID를 입력하세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loans = activeBtn.isSelected() ? svc.getActive(memberId) : svc.getHistory(memberId);
        }

        tableModel.setRowCount(0);
        for (Loan loan : loans) {
            tableModel.addRow(svc.toRow(loan));
        }

        if (loans.isEmpty()) {
            JOptionPane.showMessageDialog(this, "조회된 이력이 없습니다.", "결과 없음", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
