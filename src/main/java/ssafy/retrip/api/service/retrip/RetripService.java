package ssafy.retrip.api.service.retrip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.retrip.api.controller.image.response.ImageResponseDto;
import ssafy.retrip.domain.image.Image;
import ssafy.retrip.domain.retrip.Retrip;
import ssafy.retrip.domain.retrip.RetripRepository;
import ssafy.retrip.domain.retrip.TimeSlot;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetripService {
    private final RetripRepository retripRepository;

    // yml 설정에서 값 주입
    @Value("${retrip.image.min-count}")
    private int minImages;

    @Value("${retrip.image.max-count}")
    private int maxImages;

    // 다른 서비스에서 호출 가능하도록 public으로 변경
    @Transactional
    public Retrip createRetrip(List<Image> images, String dirName) {
        // 이미지 개수 검증
        if (images == null || images.size() < minImages) {
            throw new IllegalArgumentException("Retrip 생성을 위해서는 최소 " + minImages + "장의 이미지가 필요합니다.");
        }

        if (images.size() > maxImages) {
            log.warn("이미지가 {}장을 초과했습니다. 처음 {}장만 처리합니다.", maxImages, maxImages);
            images = images.subList(0, maxImages);
        }

        // 1. 이미지들의 시간 정렬 (여행 시작/종료 시간 결정)
        images.sort(Comparator.comparing(Image::getTakenDate));
        LocalDateTime startDate = images.get(0).getTakenDate();
        LocalDateTime endDate = images.get(images.size() - 1).getTakenDate();

        // 디버깅을 위한 로그 추가
        log.info("이미지 시간대 분석 시작 - 총 이미지 수: {}", images.size());
        for (int i = 0; i < Math.min(10, images.size()); i++) {
            LocalDateTime takenDate = images.get(i).getTakenDate();
            TimeSlot slot = TimeSlot.from(takenDate);
            log.info("이미지 {}: 시간={}, 시간대={} ({})", 
                     i, 
                     takenDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                     slot,
                     slot.getTimeRange());
        }

        // 2. 총 이동거리 계산
        double totalDistance = calculateTotalDistance(images);

        // 3. 주요 시간대 분석 (원본 시간 사용)
        TimeSlot mainTimeSlot = analyzeMainTimeSlot(images);
        log.info("주요 시간대: {} ({})", mainTimeSlot, mainTimeSlot.getTimeRange());

        // 4. 가장 많이 촬영한 장소 분석
        Map<String, Object> mainLocationInfo = analyzeMainLocation(images);
        String mainLocation = (String) mainLocationInfo.get("name");
        Double mainLocationLat = (Double) mainLocationInfo.get("latitude");
        Double mainLocationLng = (Double) mainLocationInfo.get("longitude");

        // 5. Retrip 엔티티 생성
        Retrip retrip = Retrip.builder()
                .totalDistance(totalDistance)
                .mainTimeSlot(mainTimeSlot)
                .mainLocation(mainLocation)
                .mainLocationLat(mainLocationLat)
                .mainLocationLng(mainLocationLng)
                .startDate(startDate)
                .endDate(endDate)
                .imageCount(images.size())
                .name(generateTripName(dirName, startDate))
                .description("이 여행에서는 " + mainLocation + "에서 가장 많은 사진을 찍었으며, " +
                        "주로 " + mainTimeSlot.getDescription() + "에 활동했습니다. " +
                        "총 이동거리는 " + String.format("%.2f", totalDistance) + "km입니다.")
                .build();

        // 6. Image와 Retrip 연결 (개선된 1:N 관계 활용)
        retrip.addImages(images);

        // 7. Retrip 저장
        return retripRepository.save(retrip);
    }

    // 간소화된 시간대 분석 메서드 - 원본 시간 기준
    private TimeSlot analyzeMainTimeSlot(List<Image> images) {
        Map<TimeSlot, Integer> timeSlotCounts = new EnumMap<>(TimeSlot.class);
        
        // 각 시간대별 카운트 초기화
        for (TimeSlot slot : TimeSlot.values()) {
            timeSlotCounts.put(slot, 0);
        }

        int processedImages = 0;
        int nullTimeImages = 0;
        
        // 각 이미지에 대해 시간대 분석
        for (Image image : images) {
            LocalDateTime takenDate = image.getTakenDate();
            if (takenDate != null) {
                // 원본 시간을 기준으로 TimeSlot 결정
                TimeSlot slot = TimeSlot.from(takenDate);
                timeSlotCounts.put(slot, timeSlotCounts.get(slot) + 1);
                processedImages++;
                
                // 디버깅을 위한 로그 추가 (최초 10개 이미지만)
                if (processedImages <= 10) {
                    log.info("이미지 {}: 시간={}, 시간대={}", 
                          processedImages - 1, 
                          takenDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                          slot);
                }
            } else {
                nullTimeImages++;
            }
        }
        
        // 디버깅을 위한 시간대별 카운트 로그
        log.info("시간대별 이미지 카운트 - 처리된 이미지: {}, 시간 정보 없음: {}", processedImages, nullTimeImages);
        for (TimeSlot slot : TimeSlot.values()) {
            log.info("시간대 {}: {} 장", slot, timeSlotCounts.get(slot));
        }

        // 모든 이미지에 시간 정보가 없는 경우 기본값 반환
        if (processedImages == 0) {
            log.warn("시간 정보가 있는 이미지가 없습니다. 기본 시간대(AFTERNOON)를 사용합니다.");
            return TimeSlot.AFTERNOON;
        }

        // 가장 많은 이미지가 속한 시간대 찾기
        TimeSlot mainSlot = timeSlotCounts.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(TimeSlot.AFTERNOON);
                
        log.info("선택된 주요 시간대: {} (이미지 {}장)", mainSlot, timeSlotCounts.get(mainSlot));
        return mainSlot;
    }

    //TODO CHATGPT API를 사용하여 해당 위도와 경도가 어디에서 찍은 사진인지 판단하고 name을 받아오게
    private String getLocationName(double latitude, double longitude) {
        // 여기서는 단순히 좌표를 문자열로 반환
        // TO-DO CHATGPT API를 사용하여 해당 위도와 경도가 어디에서 찍은 사진인지 판단하고 name을 받아오게
        return String.format("위도 %.4f, 경도 %.4f 부근", latitude, longitude);
    }

    //TODO CHATGPT API를 사용하여 해당 여행의 이름 생성(비전 API 사용)
    private String generateTripName(String dirName, LocalDateTime startDate) {
        String dateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

        if (dirName != null && !dirName.isEmpty()) {
            return dirName + " 여행 - " + dateStr;
        } else {
            return "여행 - " + dateStr;
        }
    }

    // Haversine 공식을 사용한 두 좌표 간 거리 계산 (km)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // 이미지들의 총 이동거리 계산
    private double calculateTotalDistance(List<Image> images) {
        double totalDistance = 0.0;

        for (int i = 0; i < images.size() - 1; i++) {
            Image current = images.get(i);
            Image next = images.get(i + 1);

            // 두 이미지 모두 위치 정보가 있는 경우에만 계산
            if (current.getLatitude() != null && current.getLongitude() != null &&
                    next.getLatitude() != null && next.getLongitude() != null) {

                double distance = calculateDistance(
                        current.getLatitude(), current.getLongitude(),
                        next.getLatitude(), next.getLongitude()
                );

                totalDistance += distance;
            }
        }

        return totalDistance;
    }

    // 장소 클러스터링 - 가장 많이 촬영한 위치 분석
    private Map<String, Object> analyzeMainLocation(List<Image> images) {
        // 위치 정보가 있는 이미지만 필터링
        List<Image> imagesWithLocation = images.stream()
                .filter(img -> img.getLatitude() != null && img.getLongitude() != null)
                .collect(Collectors.toList());

        // 위치 정보가 없으면 기본값 반환
        if (imagesWithLocation.isEmpty()) {
            Map<String, Object> defaultLocation = new HashMap<>();
            defaultLocation.put("name", "알 수 없는 위치");
            defaultLocation.put("latitude", null);
            defaultLocation.put("longitude", null);
            return defaultLocation;
        }

        // 클러스터링 (0.1km 이내는 같은 장소로 간주)
        final double CLUSTER_THRESHOLD = 0.1;
        List<List<Image>> clusters = new ArrayList<>();

        for (Image image : imagesWithLocation) {
            boolean addedToCluster = false;

            for (List<Image> cluster : clusters) {
                Image representative = cluster.get(0);
                double distance = calculateDistance(
                        representative.getLatitude(), representative.getLongitude(),
                        image.getLatitude(), image.getLongitude()
                );

                if (distance <= CLUSTER_THRESHOLD) {
                    cluster.add(image);
                    addedToCluster = true;
                    break;
                }
            }

            if (!addedToCluster) {
                List<Image> newCluster = new ArrayList<>();
                newCluster.add(image);
                clusters.add(newCluster);
            }
        }

        // 가장 큰 클러스터 찾기
        List<Image> largestCluster = clusters.stream()
                .max(Comparator.comparingInt(List::size))
                .orElse(List.of(imagesWithLocation.get(0)));

        // 클러스터의 대표 위치 계산 (평균)
        double sumLat = 0, sumLng = 0;
        for (Image img : largestCluster) {
            sumLat += img.getLatitude();
            sumLng += img.getLongitude();
        }
        double avgLat = sumLat / largestCluster.size();
        double avgLng = sumLng / largestCluster.size();

        // 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("name", getLocationName(avgLat, avgLng));
        result.put("latitude", avgLat);
        result.put("longitude", avgLng);

        return result;
    }
}
