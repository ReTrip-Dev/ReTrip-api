package ssafy.retrip.api.service.retrip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.retrip.api.service.retrip.response.ImageAnalysisResponse;
import ssafy.retrip.domain.image.Image;
import ssafy.retrip.domain.member.Member;
import ssafy.retrip.domain.member.MemberRepository;
import ssafy.retrip.domain.retrip.Retrip;
import ssafy.retrip.domain.retrip.RetripRepository;
import ssafy.retrip.domain.retrip.TimeSlot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetripService {
    private final RetripRepository retripRepository;
    private final MemberRepository memberRepository;
    private final ChatGptProxyService chatGptProxyService;

    // yml 설정에서 값 주입
    @Value("${retrip.image.min-count}")
    private int minImages;

    @Value("${retrip.image.max-count}")
    private int maxImages;

    /**
     * kakaoId를 기반으로 빈 Retrip 객체를 생성하는 메서드
     * @param memberId 회원의 카카오 ID
     * @return 생성된 Retrip 객체
     */
    @Transactional
    public Retrip createEmptyRetrip(String memberId) {
        // 카카오 ID로 회원 조회
        Member member = memberRepository.findByKakaoId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카카오 ID를 가진 회원이 존재하지 않습니다: " + memberId));
        
        log.info("회원 정보 조회 성공: kakaoId={}, email={}", memberId, member.getEmail());
        
        // 빈 Retrip 객체 생성
        Retrip retrip = Retrip.builder()
                .member(member) // 회원 설정
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .imageCount(0)
                .build();
        
        // Retrip 저장
        Retrip savedRetrip = retripRepository.save(retrip);
        log.info("빈 Retrip 생성 완료: retripId={}, memberId={}", savedRetrip.getId(), member.getId());
        
        return savedRetrip;
    }

    // 다른 서비스에서 호출 가능하도록 public으로 변경
    @Transactional
    public Retrip updateRetripWithImageData(List<Image> images, long retripId, String memberId) {
        Retrip retrip = retripRepository.findById(retripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ReTrip이 존재하지 않습니다. ID: " + retripId));

        // 1. 이미지들의 시간 정렬 (여행 시작/종료 시간 결정)
        images.sort(Comparator.comparing(Image::getTakenDate));
        retrip.setStartDate(images.get(0).getTakenDate());
        retrip.setEndDate(images.get(images.size() - 1).getTakenDate());

        // 2. 총 이동거리 계산
        double totalDistance = calculateTotalDistance(images);
        retrip.setTotalDistance(totalDistance);

        // 3. 주요 시간대 분석
        TimeSlot mainTimeSlot = analyzeMainTimeSlot(images);
        retrip.setMainTimeSlot(mainTimeSlot);

        // 4. 가장 많이 찍은 장소 분석
        Map<String, Object> mainLocationInfo = analyzeMainLocation(images);
        retrip.setMainLocation((String) mainLocationInfo.get("name"));
        retrip.setMainLocationLat((Double) mainLocationInfo.get("latitude"));
        retrip.setMainLocationLng((Double) mainLocationInfo.get("longitude"));

        // 5. 이미지 카운트
        retrip.setImageCount(images.size());

        // 6. ChatGPT API를 사용하여 여행 설명 생성
        try {
            ImageAnalysisResponse analysisResponse = chatGptProxyService.getImageAnalysis(memberId, retripId);
            
            if (analysisResponse != null && analysisResponse.getTravel_image_analysis() != null) {
                ImageAnalysisResponse.TravelAnalysis travelAnalysis = 
                    analysisResponse.getTravel_image_analysis().getTravel_analysis();
                
                if (travelAnalysis != null) {
                    // ReTrip 객체에 분석 결과 저장
                    retrip.setMbti(travelAnalysis.getMbti());
                    retrip.setOverallMood(travelAnalysis.getOverall_mood());
                    
                    // Top 방문 장소 설정
                    if (travelAnalysis.getTop_visit_place() != null) {
                        retrip.setTopVisitPlace(travelAnalysis.getTop_visit_place());
                    }
                    
                    // 추가 정보는 필요에 따라 설정
                    log.info("이미지 분석 결과 설정 완료: retripId={}", retripId);
                }
            }
        } catch (Exception e) {
            log.error("이미지 분석 결과 처리 중 오류 발생", e);
        }

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
