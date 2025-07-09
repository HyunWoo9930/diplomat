package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.enums.StampType;
import publicdata.hackathon.diplomats.domain.enums.UserLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StampEarnedResponse {
    private boolean success;
    private String message;
    private StampType stampType;
    private String stampDescription;
    private Integer stampsEarned;
    private Integer totalStamps;
    private UserLevel currentLevel;
    private String currentLevelDisplay;
    private boolean leveledUp;
    private UserLevel previousLevel;
    private String previousLevelDisplay;
    private Integer stampsToNextLevel;
}
