package ssafy.retrip.domain.retrip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetripRepository extends JpaRepository<Retrip, Long> {
    // 기본 CRUD 메서드는 JpaRepository에서 제공됨
    // 필요한 추가 쿼리 메서드는 여기에 정의
}
