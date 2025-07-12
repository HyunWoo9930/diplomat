package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import publicdata.hackathon.diplomats.domain.enums.DiscussType;

@Data
@Builder
public class DiscussBoardResponse {
	private Long id;
	private String title;
	private String content;
	private DiscussType discussType;
	private String discussTypeDisplay; // 토론 유형 한글명
	private Integer likes;
	private boolean liked; // 현재 사용자의 좋아요 상태
	private Integer viewCount;
	private String userId;
	private boolean isOwner; // 현재 사용자가 작성자인지 여부
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
