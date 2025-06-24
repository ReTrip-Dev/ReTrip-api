package ssafy.retrip.api.service.retrip;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageConverter {

  private static final int MAX_EDGE = 1080;

  /**
   * MultipartFile을 리사이징하고 JPEG byte[]로 변환합니다. (In-Memory) HEIC 포맷을 감지하여 처리합니다.
   */
  public byte[] convertAndResizeToJpeg(MultipartFile file) throws IOException {
    BufferedImage src = ImageIO.read(file.getInputStream());
    if (src == null) {
      throw new IOException("지원하지 않는 이미지 포맷이거나 파일이 손상되었습니다: " + file.getOriginalFilename());
    }

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Thumbnails.of(src)
          .size(MAX_EDGE, MAX_EDGE)
          .outputFormat("jpg")
          .outputQuality(0.9)
          .toOutputStream(out);
      return out.toByteArray();
    }
  }

  /**
   * 이미지 byte[]를 Base64 Data URL로 변환합니다. (GPT 전송용)
   */
  public String toDataUrl(byte[] imageBytes) {
    String b64 = Base64.getEncoder().encodeToString(imageBytes);
    return "data:image/jpeg;base64," + b64;
  }
}