package ssafy.retrip.api.service.image;

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

  // 최대 길이 1080px : 비율 자동 유지(keepAspectRatio = true가 기본)
  private static final int MAX_EDGE = 1080;

  public String toDataUrl(MultipartFile file) throws IOException {
    BufferedImage src = ImageIO.read(file.getInputStream());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Thumbnails.of(src)
        .size(MAX_EDGE, MAX_EDGE) // 긴 변이 1080px 이하로
        .outputFormat("jpg")
        .outputQuality(0.9)
        .toOutputStream(out);

    String b64 = Base64.getEncoder().encodeToString(out.toByteArray());
    return "data:image/jpeg;base64," + b64;
  }
}

