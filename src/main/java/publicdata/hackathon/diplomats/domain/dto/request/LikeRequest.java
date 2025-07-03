package publicdata.hackathon.diplomats.domain.dto.request;

import lombok.Data;

@Data
public class LikeRequest {
    private String targetType; // "FreeBoard", "DiscussBoard", "Diary"
    private Long targetId;      // 게시글 ID
}
