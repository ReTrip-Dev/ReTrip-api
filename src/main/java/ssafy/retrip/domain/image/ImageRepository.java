package ssafy.retrip.domain.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    
    // 디렉토리명으로 이미지 찾기
    List<Image> findByDirName(String dirName);
    
    // 위치 정보가 있는 이미지 찾기
    List<Image> findByLatitudeIsNotNullAndLongitudeIsNotNull();
    
    // 촬영 날짜 기준으로 이미지 찾기
    List<Image> findByTakenDateBetween(LocalDateTime start, LocalDateTime end);
}
