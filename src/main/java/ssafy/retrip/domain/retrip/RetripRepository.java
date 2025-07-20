package ssafy.retrip.domain.retrip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetripRepository extends JpaRepository<Retrip, Long> {

}
