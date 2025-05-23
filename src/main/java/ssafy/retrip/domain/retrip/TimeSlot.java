package ssafy.retrip.domain.retrip;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum TimeSlot {
    DAWN(0, 5, "새벽(00:00-06:00)"),
    MORNING(6, 11, "오전(06:00-12:00)"),
    AFTERNOON(12, 17, "오후(12:00-18:00)"),
    NIGHT(18, 23, "밤(18:00-24:00)");

    private final int startHour;
    private final int endHour;
    private final String description;

    TimeSlot(int startHour, int endHour, String description) {
        this.startHour = startHour;
        this.endHour = endHour;
        this.description = description;
    }

    /**
     * 주어진 시간이 이 시간대에 속하는지 확인
     */
    public boolean contains(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        return hour >= startHour && hour <= endHour;
    }

    /**
     * 주어진 시간에 해당하는 시간대 반환
     */
    public static TimeSlot from(LocalDateTime dateTime) {
        if (dateTime == null) {
            log.warn("시간 정보가 없습니다. 기본 시간대(AFTERNOON)를 사용합니다.");
            return AFTERNOON;
        }
        
        int hour = dateTime.getHour();
        return fromHour(hour);
    }
    
    /**
     * 시간값(0-23)을 기준으로 TimeSlot 반환
     */
    private static TimeSlot fromHour(int hour) {
        if (hour < 0 || hour > 23) {
            log.warn("비정상적인 시간값: {}. 범위(0-23)를 벗어났습니다. 기본값 AFTERNOON 사용.", hour);
            return AFTERNOON;
        }
        
        for (TimeSlot slot : values()) {
            if (hour >= slot.startHour && hour <= slot.endHour) {
                return slot;
            }
        }
        
        // 기본값으로 AFTERNOON 반환 (비정상 케이스)
        log.error("시간대 결정 로직 오류. 시간값 {}에 해당하는 시간대를 찾지 못했습니다.", hour);
        return AFTERNOON;
    }

    /**
     * 시간대에 대한 설명 반환
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 디버깅용 시간 정보 출력
     */
    public String getTimeRange() {
        return String.format("%02d:00-%02d:59", startHour, endHour);
    }
}
