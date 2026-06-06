package library.ui;

import library.model.Loan;
import library.service.HistorySvc;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 대출 목록 표시 공용 컴포넌트.
 * MyLoansUI(내 대출현황)와 ReturnUI(반납)가 공유한다.
 * 행 변환·상태 포맷·제목 조회·선택 처리를 이 한 곳에 모은다(중복 제거).
 */
public class LoanTablePanel extends JPanel {

    private final HistorySvc svc;

    private static final String[] COLUMNS =
            {"대출ID", "제목", "ISBN", "대출일", "반납기한", "반납일", "상태"};

    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public LoanTablePanel(HistorySvc svc) {
        this.svc = svc;
        setLayout(new BorderLayout());
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(640, 280)); // 리스트가 충분히 보이도록
        add(sp, BorderLayout.CENTER);
    }

    /** 대출 목록으로 테이블을 채운다(행 변환·상태 포맷은 HistorySvc.toRow에 위임). */
    public void setLoans(List<Loan> loans) {
        model.setRowCount(0);
        for (Loan l : loans) model.addRow(svc.toRow(l));
    }

    /** 선택된 행들의 대출ID. */
    public List<Integer> getSelectedLoanIds() {
        return idsOf(table.getSelectedRows());
    }

    /** 현재 표시 중인 전체 행의 대출ID. */
    public List<Integer> getAllLoanIds() {
        int[] all = new int[model.getRowCount()];
        for (int i = 0; i < all.length; i++) all[i] = i;
        return idsOf(all);
    }

    public int getRowCount() { return model.getRowCount(); }

    private List<Integer> idsOf(int[] rows) {
        List<Integer> ids = new ArrayList<>();
        for (int r : rows) ids.add((Integer) model.getValueAt(r, 0));
        return ids;
    }
}
