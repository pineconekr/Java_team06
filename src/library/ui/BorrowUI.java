package library.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import library.service.BorrowSvc;
import library.model.BorrowResult;

public class BorrowUI extends JFrame {

    private BorrowSvc borrowSvc;

    public BorrowUI(BorrowSvc borrowSvc) {
        this.borrowSvc = borrowSvc;

        setTitle("도서 대출 처리");
        setSize(400, 300);
        setLocationRelativeTo(null); // 메인 창 위에 정중앙 팝업
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 이 창만 닫기 설정

        Container c = getContentPane();
        c.setLayout(new GridLayout(4, 1, 10, 10));
        c.setBackground(Color.WHITE);
        ((JPanel)c).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. 회원 ID 입력 행
        JPanel panelMember = new JPanel(new BorderLayout(10, 0));
        panelMember.setOpaque(false);
        panelMember.add(new JLabel("회원 ID : ", JLabel.RIGHT), BorderLayout.WEST);
        JTextField tfMemberId = new JTextField();
        panelMember.add(tfMemberId, BorderLayout.CENTER);

        // 2. 도서 ISBN 입력 행
        JPanel panelBook = new JPanel(new BorderLayout(10, 0));
        panelBook.setOpaque(false);
        panelBook.add(new JLabel("도서 ISBN : ", JLabel.RIGHT), BorderLayout.WEST);
        JTextField tfIsbn = new JTextField();
        panelBook.add(tfIsbn, BorderLayout.CENTER);

        // 3. 대출 신청 버튼
        JButton btnSubmit = new JButton("대출 신청");
        btnSubmit.setBackground(new Color(33, 102, 224)); // 메인화면 대출 버튼과 색상 맞춤
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        c.add(panelMember);
        c.add(panelBook);
        c.add(btnSubmit);

        // 4. 대출 신청 버튼 클릭 이벤트
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String memberId = tfMemberId.getText().trim();
                String isbn = tfIsbn.getText().trim();

                // 빈칸 검사
                if (memberId.isEmpty() || isbn.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "회원 ID와 ISBN을 모두 입력해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 대출 로직 호출!
                BorrowResult result = borrowSvc.borrow(memberId, isbn);

                // 결과에 따른 맞춤형 메시지창 띄우기
                switch (result) {
                    case SUCCESS -> {
                        JOptionPane.showMessageDialog(null, "대출이 성공적으로 완료되었습니다!", "성공", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // 대출 창 닫기
                    }
                    case FAIL_NOT_AVAILABLE -> {
                        JOptionPane.showMessageDialog(null, "대출 불가: 존재하지 않는 도서이거나 이미 대출 중입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                    case FAIL_SUSPENDED -> {
                        JOptionPane.showMessageDialog(null, "대출 불가: 연체 패널티로 인해 정지된 회원입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                    case FAIL_LIMIT_EXCEEDED -> {
                        JOptionPane.showMessageDialog(null, "대출 불가: 등급별 대출 권수 한도를 초과했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        setVisible(true);
    }
}
