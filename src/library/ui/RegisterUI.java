package library.ui;

import library.model.Member;
import library.repository.IMemberRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

public class RegisterUI extends JFrame {

    private IMemberRepository memberRepo;

    public RegisterUI(IMemberRepository memberRepo) {
        this.memberRepo = memberRepo;

        setTitle("신규 회원가입");
        setSize(350, 250);
        setLocationRelativeTo(null); // 화면 정중앙에 팝업 띄우기
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 가입 창만 닫히게 설정

        Container c = getContentPane();
        c.setLayout(new GridLayout(4, 1, 10, 10));
        c.setBackground(Color.WHITE);
        ((JPanel)c).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. 학번(ID) 입력 칸
        JPanel idPanel = new JPanel(new BorderLayout(10, 0));
        idPanel.setOpaque(false);
        JLabel lblId = new JLabel("학번 (ID) : ", JLabel.RIGHT);
        lblId.setPreferredSize(new Dimension(80, 30));
        idPanel.add(lblId, BorderLayout.WEST);
        JTextField tfId = new JTextField();
        idPanel.add(tfId, BorderLayout.CENTER);

        // 2. 이름 입력 칸
        JPanel namePanel = new JPanel(new BorderLayout(10, 0));
        namePanel.setOpaque(false);
        JLabel lblName = new JLabel("이름 (Name) : ", JLabel.RIGHT);
        lblName.setPreferredSize(new Dimension(80, 30));
        namePanel.add(lblName, BorderLayout.WEST);
        JTextField tfName = new JTextField();
        namePanel.add(tfName, BorderLayout.CENTER);

        // 3. 가입 버튼
        JButton btnSubmit = new JButton("가입하기");
        btnSubmit.setBackground(new Color(12, 45, 74)); // 상단 네이비 바와 색상 맞춤
        btnSubmit.setForeground(Color.black);
        btnSubmit.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        c.add(idPanel);
        c.add(namePanel);
        c.add(new JLabel()); // 줄 맞춤용 빈 공간
        c.add(btnSubmit);

        // 4. 가입 버튼 클릭 이벤트 (DB 연동)
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = tfId.getText().trim();
                String name = tfName.getText().trim();

                // 빈칸 검사
                if (id.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "학번과 이름을 모두 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 중복 가입 검사
                Optional<Member> existingMember = memberRepo.findById(id);
                if (existingMember.isPresent()) {
                    JOptionPane.showMessageDialog(null, "이미 가입된 학번입니다.", "가입 실패", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 새로운 멤버 객체 생성 후 DB 저장소에 추가
                Member newMember = new Member(id, name);
                memberRepo.add(newMember);

                // 성공 메시지 띄우고 창 닫기
                JOptionPane.showMessageDialog(null, "[" + name + "]님 환영합니다!\n회원가입이 성공적으로 완료되었습니다.", "가입 성공", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });

        setVisible(true);
    }
}