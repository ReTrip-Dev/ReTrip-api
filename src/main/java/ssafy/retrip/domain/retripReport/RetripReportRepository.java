package ssafy.retrip.domain.retripReport;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RetripReportRepository extends JpaRepository<RetripReport, Long> {

  @Query(nativeQuery = true,
      value = "SELECT * "
              + "FROM retrip_reports "
              + "WHERE member_id = :memberId"
  )
  Optional<List<RetripReport>> findByMemberId(String memberId);

}
