package ssafy.retrip.api.controller.retrip;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ssafy.retrip.api.controller.retrip.response.TravelAnalysisResponseDto;
import ssafy.retrip.api.service.retrip.RetripService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class RetripController {

  private final RetripService retripService;

  @PostMapping("/uploads")
  public CompletableFuture<ResponseEntity<TravelAnalysisResponseDto>> uploadMultipleImages(HttpServletRequest request,
      @RequestParam("images") List<MultipartFile> images) throws IOException {

    return retripService.createRetripFromImages(images)
        .thenApply(result -> {
          log.info("여행 분석 완료: retripId={}", result.getRetripId());
          return ResponseEntity.ok(result);
        })
        .exceptionally(ex -> {
          log.error("여행 분석 처리 중 오류 발생", ex);
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(null);
        });
  }
}
