package publicdata.hackathon.diplomats.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserLevel {
    LEVEL_1(1, "Lv.1 시민외교 새싹", 0, 9),
    LEVEL_2(2, "Lv.2 시민외교 꿈나무", 10, 19),
    LEVEL_3(3, "Lv.3 시민외교 리더", 20, 29),
    LEVEL_4(4, "Lv.4 시민외교 전문가", 30, 39),
    LEVEL_5(5, "Lv.5 시민외교 대장", 40, Integer.MAX_VALUE);

    private final int level;
    private final String displayName;
    private final int minStamps;
    private final int maxStamps;

    UserLevel(int level, String displayName, int minStamps, int maxStamps) {
        this.level = level;
        this.displayName = displayName;
        this.minStamps = minStamps;
        this.maxStamps = maxStamps;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 스탬프 개수에 따른 레벨을 반환
     */
    public static UserLevel fromStampCount(int stampCount) {
        for (UserLevel level : UserLevel.values()) {
            if (stampCount >= level.minStamps && stampCount <= level.maxStamps) {
                return level;
            }
        }
        return LEVEL_1; // 기본값
    }

    /**
     * 다음 레벨까지 필요한 스탬프 개수
     */
    public int getStampsToNextLevel(int currentStamps) {
        if (this == LEVEL_5) {
            return 0; // 최고 레벨
        }
        
        UserLevel nextLevel = UserLevel.values()[this.ordinal() + 1];
        return nextLevel.minStamps - currentStamps;
    }
}
