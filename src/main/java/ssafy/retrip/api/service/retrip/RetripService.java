package ssafy.retrip.api.service.retrip;

import static ssafy.retrip.utils.CoordinateUtil.calculateAverageCoordinates;
import static ssafy.retrip.utils.ImageMetaDataUtil.extractMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ssafy.retrip.api.controller.retrip.response.TravelAnalysisResponseDto;
import ssafy.retrip.api.service.openai.GptImageAnalysisService;
import ssafy.retrip.api.service.retrip.info.GpsCoordinate;
import ssafy.retrip.api.service.retrip.info.ImageMetaData;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetripService {

  private final ImageConverter imageConverter;
  private final GptImageAnalysisService gptImageAnalysisService;
  private final RetripPersistenceService retripPersistenceService;

  @Value("${retrip.image.min-count}")
  private int minImageCount;

  @Value("${retrip.image.max-count}")
  private int maxImageCount;

  public CompletableFuture<TravelAnalysisResponseDto> createRetripFromImages(List<MultipartFile> images) {

    validateMinimumImageCount(images);

    images = adjustImageCountToMaximum(images);
    List<String> imageDataUrlsForGpt = new ArrayList<>();
    List<ImageMetaData> allMetadata = new ArrayList<>();

    prepareImageForProcessing(images, allMetadata, imageDataUrlsForGpt);

    GpsCoordinate averageCoords = calculateAverageCoordinates(allMetadata);

    return gptImageAnalysisService.analyze(imageDataUrlsForGpt, averageCoords.latitude(), averageCoords.longitude())
        .thenApply(analysisResponse -> {
          try {
            log.info("GPT 분석 완료, Retrip 저장 시작");
            return retripPersistenceService.saveRetrip(analysisResponse, allMetadata);
          } catch (Exception e) {
            log.error("GPT 분석 결과 처리 및 Retrip 생성 중 오류 발생", e);
            throw new RuntimeException("여행 기록 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
          }
        })
        .exceptionally(throwable -> {
          log.error("전체 처리 과정에서 오류 발생", throwable);
          throw new RuntimeException("여행 분석 처리 중 오류가 발생했습니다: " + throwable.getMessage(), throwable);
        });
  }

  private void validateMinimumImageCount(List<MultipartFile> images) {
    if (images == null || images.size() < minImageCount) {
      throw new IllegalArgumentException("이미지는 최소 " + minImageCount + "장 이상 업로드해야 합니다.");
    }
  }

  private List<MultipartFile> adjustImageCountToMaximum(List<MultipartFile> images) {
    return images.size() <= maxImageCount ? images : images.subList(0, maxImageCount);
  }

  private void prepareImageForProcessing(
      List<MultipartFile> images,
      List<ImageMetaData> allMetadata,
      List<String> imageDataUrlsForGpt
  ) {
    for (MultipartFile image : images) {
      if (image.isEmpty()) {
        continue;
      }
      try {
        allMetadata.add(extractMetadata(image.getInputStream()));
        byte[] resizedImageBytes = imageConverter.convertAndResizeToJpeg(image);
        imageDataUrlsForGpt.add(imageConverter.toDataUrl(resizedImageBytes));
      } catch (Exception e) {
        log.error("이미지 처리 중 오류 발생: {}", image.getOriginalFilename(), e);
      }
    }

    if (allMetadata.size() < minImageCount) {
      throw new IllegalStateException("유효한 이미지가 " + minImageCount + "장 미만입니다.");
    }
  }
}