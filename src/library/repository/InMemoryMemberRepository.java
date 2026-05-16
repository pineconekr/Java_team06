package library.repository;

import library.model.Member;
import library.model.MemberGrade;
import java.util.*;

public class InMemoryMemberRepository implements IMemberRepository {
    private static final InMemoryMemberRepository INSTANCE = new InMemoryMemberRepository();
    private final Map<String, Member> members = new LinkedHashMap<>();

    private InMemoryMemberRepository() {
        add(new Member("M001", "김철수"));
        Member m2 = new Member("M002", "이영희");
        m2.setGrade(MemberGrade.PREMIUM);
        add(m2);
        add(new Member("M003", "박민준"));
    }

    public static InMemoryMemberRepository getInstance() { return INSTANCE; }

    @Override public void add(Member member) { members.put(member.getMemberId(), member); }
    @Override public Optional<Member> findById(String id) { return Optional.ofNullable(members.get(id)); }
    @Override public List<Member> findAll() { return new ArrayList<>(members.values()); }
}
