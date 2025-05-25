package ssafy.retrip.domain.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByKakaoId(String kakaoId);

  Optional<Member> findByUserId(String userId);

  boolean existsByUserId(String userId);
}
