package ssafy.retrip.api.controller.retrip;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ssafy.retrip.api.controller.retrip.response.TravelAnalysisResponseDto;
import ssafy.retrip.api.service.retrip.RetripService;
import ssafy.retrip.domain.member.MemberRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class RetripController {

  private final RetripService retripService;

  @PostMapping("/uploads")
  public ResponseEntity<TravelAnalysisResponseDto> uploadMultipleImages(HttpServletRequest request,
      @RequestParam("images") List<MultipartFile> images) throws IOException {
    // 비회원 사용자를 가정하여 memberId를 null로 설정합니다.
    String memberId = null;

    try {
      return ResponseEntity.ok(retripService.createRetripFromImages(images, memberId));
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // TO-DO : retrip 히스토리 조회 + 회원가입 기능


}
