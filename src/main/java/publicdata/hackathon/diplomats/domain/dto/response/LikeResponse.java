package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LikeResponse {
    private boolean isLiked;    // 현재 사용자가 좋아요를 눌렀는지
    private long likeCount;     // 총 좋아요 개수
    private String message;     // 결과 메시지
}
