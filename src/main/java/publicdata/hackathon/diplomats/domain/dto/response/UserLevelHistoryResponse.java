package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.enums.UserLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLevelHistoryResponse {
    private Long id;
    private UserLevel previousLevel;
    private String previousLevelDisplay;
    private UserLevel newLevel;
    private String newLevelDisplay;
    private Integer stampCount;
    private LocalDateTime levelUpAt;
}
