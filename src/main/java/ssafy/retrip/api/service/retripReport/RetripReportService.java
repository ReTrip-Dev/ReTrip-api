package ssafy.retrip.api.service.retripReport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.retrip.domain.retripReport.RetripReport;
import ssafy.retrip.domain.retripReport.RetripReportRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RetripReportService {

  private final RetripReportRepository retripReportRepository;

  @Transactional
  public void saveReportImage(String memberId, String imageUrl) {

    RetripReport report = RetripReport.builder()
        .memberId(memberId)
        .imageUrl(imageUrl).build();

    retripReportRepository.save(report);
  }
}