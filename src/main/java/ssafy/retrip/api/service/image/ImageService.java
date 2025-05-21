package ssafy.retrip.api.service.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ssafy.retrip.aws.S3Uploader;
import ssafy.retrip.api.controller.image.response.ImageResponseDto;
import ssafy.retrip.domain.image.Image;
import ssafy.retrip.domain.image.ImageRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Uploader s3Uploader;
    private final ImageRepository imageRepository;

    @Transactional
    public List<ImageResponseDto> uploadImages(List<MultipartFile> images, String dirName) throws IOException {
        List<ImageResponseDto> uploadedImages = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (MultipartFile image : images) {
            try {
                if (!image.isEmpty()) {
                    // 파일을 로컬에 임시 저장 (HEIC 처리 포함)
                    Optional<File> convertedFile = processAndConvertFile(image);
                    
                    if (convertedFile.isPresent()) {
                        File file = convertedFile.get();
                        
                        // 메타데이터 추출
                        ImageMetadata metadata = extractMetadata(file);
                        
                        // 메타데이터가 없을 경우 기본값 사용
                        if (metadata.takenDate == null) {
                            log.warn("이미지에 촬영 시간 정보가 없습니다: {}. 현재 시간을 사용합니다.", image.getOriginalFilename());
                            metadata.takenDate = LocalDateTime.now();
                        }
                        
                        if (metadata.latitude == null || metadata.longitude == null) {
                            log.warn("이미지에 위치 정보가 없습니다: {}. 가능한 경우 파일명 또는 다른 메타데이터에서 추출합니다.", image.getOriginalFilename());
                            // 파일명에서 위치 정보 추출 시도
                            extractLocationFromFilename(image.getOriginalFilename(), metadata);
                        }
                        
                        // S3에 업로드
                        String storedFileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                        String imageUrl = s3Uploader.upload(image, dirName);
                        
                        // DB에 이미지 정보 저장
                        Image savedImage = imageRepository.save(Image.builder()
                                .imageUrl(imageUrl)
                                .originalFileName(image.getOriginalFilename())
                                .storedFileName(storedFileName)
                                .contentType(image.getContentType())
                                .fileSize(image.getSize())
                                .takenDate(metadata.takenDate)
                                .location(metadata.location)
                                .latitude(metadata.latitude)
                                .longitude(metadata.longitude)
                                .dirName(dirName)
                                .build());
                        
                        uploadedImages.add(ImageResponseDto.from(savedImage));
                        
                        // 파일 처리 후 임시 파일 삭제
                        try {
                            file.delete();
                        } catch (Exception e) {
                            log.warn("임시 파일 삭제 실패: {}", e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("이미지 처리 중 오류 발생: {}", image.getOriginalFilename(), e);
                errors.add(image.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            log.warn("일부 이미지 처리 실패: {}", errors);
        }
        
        return uploadedImages;
    }
    
    // 파일 처리 및 변환 메소드 - HEIC 파일 처리 포함
    private Optional<File> processAndConvertFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return Optional.empty();
        }
        
        // HEIC 파일 처리
        if (filename.toLowerCase().endsWith(".heic") || 
            (file.getContentType() != null && file.getContentType().toLowerCase().contains("heic"))) {
            log.info("HEIC 파일 변환 시도: {}", filename);
            
            try {
                // HEIC 파일을 임시 파일로 저장
                File tempHeicFile = File.createTempFile("heic_", ".heic");
                file.transferTo(tempHeicFile);
                
                // JPEG로 변환 시도 (TwelveMonkeys에 의존)
                File jpegFile = File.createTempFile("converted_", ".jpg");
                
                try {
                    // ImageIO를 사용하여 변환 시도
                    BufferedImage image = ImageIO.read(tempHeicFile);
                    if (image != null) {
                        Thumbnails.of(image)
                            .scale(1.0)
                            .outputFormat("jpg")
                            .toFile(jpegFile);
                        
                        tempHeicFile.delete();
                        return Optional.of(jpegFile);
                    }
                } catch (Exception e) {
                    log.warn("HEIC 변환 실패: {}. 대체 방법 시도", e.getMessage());
                    
                    // 대체 방법: 원본 파일 사용
                    tempHeicFile.delete();
                    jpegFile.delete();
                    
                    // 원본을 JPEG로 간주하고 그대로 처리
                    File standardFile = File.createTempFile("original_", ".jpg");
                    file.transferTo(standardFile);
                    return Optional.of(standardFile);
                }
            } catch (Exception e) {
                log.error("HEIC 파일 처리 중 오류 발생: {}", e.getMessage());
            }
        }
        
        // 일반 파일 처리 (기존 로직)
        return s3Uploader.convert(file);
    }

    // 파일명에서 위치 정보 추출 시도
    private void extractLocationFromFilename(String filename, ImageMetadata metadata) {
        // 예: "Seoul_37.5665_126.9780.jpg" 형식의 파일명에서 위치 정보 추출
        try {
            String[] parts = filename.split("_");
            if (parts.length >= 3) {
                Double lat = Double.parseDouble(parts[parts.length - 2]);
                Double lng = Double.parseDouble(parts[parts.length - 1].split("\\.")[0]);
                
                // 유효한 좌표값 확인
                if (isValidCoordinate(lat, lng)) {
                    metadata.latitude = lat;
                    metadata.longitude = lng;
                    metadata.location = "위도: " + lat + ", 경도: " + lng + " (파일명에서 추출)";
                }
            }
        } catch (Exception e) {
            log.debug("파일명에서 위치 정보 추출 실패: {}", filename);
        }
    }

    private boolean isValidCoordinate(Double lat, Double lng) {
        return lat != null && lng != null && 
               lat >= -90 && lat <= 90 && 
               lng >= -180 && lng <= 180;
    }

    private ImageMetadata extractMetadata(File imageFile) {
        ImageMetadata metadata = new ImageMetadata();
        
        try {
            Metadata rawMetadata = ImageMetadataReader.readMetadata(imageFile);
            
            // 1. 촬영 시간 추출 - 여러 태그로 시도
            extractDateTimeInfo(rawMetadata, metadata);
            
            // 2. 위치 정보 추출 - GPS 데이터
            extractGpsInfo(rawMetadata, metadata);
            
            // 3. 다른 유용한 메타데이터 추출 (향후 확장)
            
        } catch (ImageProcessingException | IOException e) {
            log.error("이미지 메타데이터 추출 중 오류 발생", e);
        }
        
        return metadata;
    }

    private void extractDateTimeInfo(Metadata rawMetadata, ImageMetadata metadata) {
        // EXIF 디렉토리에서 날짜 추출 시도
        ExifSubIFDDirectory exifDirectory = rawMetadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (exifDirectory != null) {
            // 1. 원본 촬영 시간 태그
            Date date = exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            
            // 2. 다른 날짜 관련 태그 시도
            if (date == null) {
                date = exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME);
            }
            if (date == null) {
                date = exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
            }
            
            if (date != null) {
                metadata.takenDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            }
        }
    }

    private void extractGpsInfo(Metadata rawMetadata, ImageMetadata metadata) {
        GpsDirectory gpsDirectory = rawMetadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDirectory != null) {
            GeoLocation geoLocation = gpsDirectory.getGeoLocation();
            if (geoLocation != null && !geoLocation.isZero()) {
                metadata.latitude = geoLocation.getLatitude();
                metadata.longitude = geoLocation.getLongitude();
                metadata.location = "위도: " + metadata.latitude + ", 경도: " + metadata.longitude;
            }
        }
    }

    // 메타데이터 저장을 위한 내부 클래스
    private static class ImageMetadata {
        LocalDateTime takenDate;
        String location;
        Double latitude;
        Double longitude;
        
        // 메타데이터가 완전한지 확인
        public boolean isComplete() {
            return takenDate != null && latitude != null && longitude != null;
        }
    }
}
