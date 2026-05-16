package library.repository;

import library.model.Member;
import java.util.List;
import java.util.Optional;

public interface IMemberRepository {
    void add(Member member);
    Optional<Member> findById(String memberId);
    List<Member> findAll();
}
