package ssafy.retrip.api.controller.image;

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
import ssafy.retrip.api.controller.image.response.ImageResponseDto;
import ssafy.retrip.api.controller.image.response.TravelAnalysisResponseDto;
import ssafy.retrip.api.service.image.ImageService;
import ssafy.retrip.domain.retrip.Retrip;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/uploads")
    public ResponseEntity<List<ImageResponseDto>> uploadMultipleImages(HttpServletRequest request,
            @RequestParam("images") List<MultipartFile> images) throws IOException {
        //HttpSession session = request.getSession();
        //String memberId = String.valueOf(session.getAttribute("member"));
        String memberId = "4277332119";

        try {
            System.out.println("✅ 업로드 시작, 파일 수: " + images.size());
            List<ImageResponseDto> uploadedImages = imageService.uploadImages(images, memberId);
            return ResponseEntity.ok(uploadedImages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        //TODO 아래와 같은 예시로 변경해주시면 됩니다.

//        try {
//            List<ImageResponseDto> uploadedImages = imageService.uploadImages(images, memberId);
//
//            // 분석 데이터와 Retrip 객체 가져오기
//            Retrip retrip = retripService.getLatestRetripByMemberId(memberId);
//            TravelImageAnalysis analysis = retripService.getTravelAnalysis(retrip.getId());
//
//            // 응답 DTO 생성
//            TravelAnalysisResponseDto responseDto = TravelAnalysisResponseDto.from(analysis, retrip, username);
//            return ResponseEntity.ok(responseDto);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }


    }
}
