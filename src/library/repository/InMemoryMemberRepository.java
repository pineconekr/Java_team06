package library.repository;

import library.model.Member;
import library.model.MemberGrade;
import java.util.*;

public class InMemoryMemberRepository implements IMemberRepository {
    private static final InMemoryMemberRepository INSTANCE = new InMemoryMemberRepository();
    private final Map<String, Member> members = new LinkedHashMap<>();

    private InMemoryMemberRepository() {
        // DB 형식과 일관성을 유지하기 위해 학번 형식(8자리)으로 수정
        add(new Member("20240001", "김철수"));

        Member m2 = new Member("20240002", "이영희");
        m2.setGrade(MemberGrade.PREMIUM);
        add(m2);

        add(new Member("20240003", "박민준"));
    }

    public static InMemoryMemberRepository getInstance() { return INSTANCE; }

    @Override
    public void add(Member member) {
        members.put(member.getMemberId(), member);
    }

    @Override
    public Optional<Member> findById(String id) {
        return Optional.ofNullable(members.get(id));
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(members.values());
    }
}