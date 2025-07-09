package publicdata.hackathon.diplomats.domain.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.enums.UserLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLevelResponse {
    
    private String userName;
    private Integer totalStamps;
    private UserLevel currentLevel;
    private String currentLevelDisplay;
    private Integer stampsToNextLevel;
    private Boolean isMaxLevel;
    
    private List<UserStampResponse> recentStamps;
    private List<UserLevelHistoryResponse> levelHistory;
    
    // 상세 스탬프 통계
    private StampStatisticsResponse stampStatistics;
    
    // 일자별 스탬프 히스토리
    private List<DailyStampHistoryResponse> dailyStampHistory;
}
