package ssafy.retrip.api.controller.image;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ssafy.retrip.api.controller.image.response.TravelAnalysisResponseDto;
import ssafy.retrip.api.service.image.ImageService;
import ssafy.retrip.api.service.retrip.RetripService;
import ssafy.retrip.api.service.retripReport.RetripReportService;
import ssafy.retrip.aws.S3Uploader;
import ssafy.retrip.domain.member.MemberRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

  private final S3Uploader s3Uploader;
  private final ImageService imageService;
  private final RetripService retripService;
  private final MemberRepository memberRepository;
  private final RetripReportService retripReportService;

  @PostMapping("/uploads")
  public ResponseEntity<TravelAnalysisResponseDto> uploadMultipleImages(HttpServletRequest request,
      @RequestParam("images") List<MultipartFile> images) throws IOException {
    //HttpSession session = request.getSession();
    //String memberId = String.valueOf(session.getAttribute("member"));
    String memberId = "4268383655";

    try {
      return ResponseEntity.ok(imageService.uploadImages(images, memberId));
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/retrip")
  public ResponseEntity<String> uploadRetripImage(HttpServletRequest request,
      @RequestParam("image") MultipartFile image,
      @RequestParam("retripId") String retripId) throws IOException {

    //HttpSession session = request.getSession();
    //String memberId = String.valueOf(session.getAttribute("member"));
    String memberId = "4268383655";
    String dirName = "retrip/" + memberId + "/" + retripId;
    String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();

    String uploadUrl = s3Uploader.upload(image, dirName, fileName);
    retripReportService.saveReportImage(memberId, uploadUrl);

    return ResponseEntity.ok("success");
  }
}
