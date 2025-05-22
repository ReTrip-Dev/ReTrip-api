package ssafy.retrip.api.controller.image;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ssafy.retrip.api.controller.image.response.ImageResponseDto;
import ssafy.retrip.api.service.image.ImageService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/uploads")
    public ResponseEntity<List<ImageResponseDto>> uploadMultipleImages(
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(value = "dirName", defaultValue = "images") String dirName) throws IOException {
        
        List<ImageResponseDto> uploadedImages = imageService.uploadImages(images, dirName);
        return ResponseEntity.ok(uploadedImages);
    }
}
