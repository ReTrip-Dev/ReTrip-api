package ssafy.retrip.api.controller.image.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.domain.image.Image;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageResponseDto {
    private Long id;
    private String imageUrl;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime takenDate;
    private String location;
    private Double latitude;
    private Double longitude;
    
    public static ImageResponseDto from(Image image) {
        return ImageResponseDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .originalFileName(image.getOriginalFileName())
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .takenDate(image.getTakenDate())
                .location(image.getLocation())
                .latitude(image.getLatitude())
                .longitude(image.getLongitude())
                .build();
    }
}
