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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class RetripController {

  private final RetripService retripService;

  @PostMapping("/uploads")
  public ResponseEntity<TravelAnalysisResponseDto> uploadMultipleImages(HttpServletRequest request,
      @RequestParam("images") List<MultipartFile> images) throws IOException {

    try {
      return ResponseEntity.ok(retripService.createRetripFromImages(images));
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
