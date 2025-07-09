package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.enums.UserLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPageResponse {
    
    private String userId;
    private String maskedPassword;
    private UserLevel currentLevel;
    private String currentLevelDisplay;
    private Integer totalStamps;
    private Integer stampsToNextLevel;
    private String citizenType; // 시민력 테스트 결과 (예: "평화중재형")
    private Boolean isMaxLevel;
}
