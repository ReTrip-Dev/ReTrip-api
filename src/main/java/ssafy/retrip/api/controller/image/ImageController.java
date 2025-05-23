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
import ssafy.retrip.api.service.image.ImageService;

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
        String memberId = "1234";

        try {
            System.out.println("✅ 업로드 시작, 파일 수: " + images.size());
            List<ImageResponseDto> uploadedImages = imageService.uploadImages(images, memberId);
            return ResponseEntity.ok(uploadedImages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

//        List<ImageResponseDto> uploadedImages = imageService.uploadImages(images, memberId);
//        return ResponseEntity.ok(uploadedImages);
    }
}
