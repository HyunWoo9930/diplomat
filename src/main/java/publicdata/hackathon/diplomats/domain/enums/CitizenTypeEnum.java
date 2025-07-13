package publicdata.hackathon.diplomats.domain.enums;

import lombok.Getter;

@Getter
public enum CitizenTypeEnum {
    DIGITAL_COMMUNICATION("디지털소통형"),
    PEACE_MEDIATION("평화중재형"),
    CULTURAL_DIPLOMACY("문화외교형"),
    ECONOMIC_TRADE("경제통상형"),
    CLIMATE_ACTION("기후행동형");

    private final String displayName;

    CitizenTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 영어 타입명을 한글 표시명으로 변환
     */
    public static String toDisplayName(String englishType) {
        if (englishType == null || englishType.trim().isEmpty()) {
            return "미진단";
        }
        
        try {
            CitizenTypeEnum type = CitizenTypeEnum.valueOf(englishType.trim());
            return type.getDisplayName();
        } catch (IllegalArgumentException e) {
            // 매치되지 않는 경우 원본 반환 또는 기본값
            return "미진단";
        }
    }

    /**
     * 한글 표시명을 영어 타입명으로 변환
     */
    public static String toEnglishType(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }
        
        for (CitizenTypeEnum type : values()) {
            if (type.getDisplayName().equals(displayName.trim())) {
                return type.name();
            }
        }
        return null;
    }
}
