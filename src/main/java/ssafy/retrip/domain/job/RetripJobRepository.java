package ssafy.retrip.domain.job;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetripJobRepository extends JpaRepository<RetripJob, Long> {

  Optional<RetripJob> findByJobId(String jobId);
}
