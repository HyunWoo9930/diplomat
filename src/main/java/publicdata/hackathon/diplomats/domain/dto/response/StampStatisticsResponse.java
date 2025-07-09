package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StampStatisticsResponse {
    
    private Integer totalStamps;
    private Integer diaryWriteStamps;      // 실천일지 작성으로 얻은 스탬프
    private Integer diaryLikeStamps;       // 좋아요로 얻은 스탬프  
    private Integer voteStamps;            // 투표로 얻은 스탬프
}
