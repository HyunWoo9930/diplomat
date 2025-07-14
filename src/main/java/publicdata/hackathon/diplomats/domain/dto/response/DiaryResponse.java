package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiaryResponse {
	private Long id;
	private String title;
	private String description;
	private String action;
	private Integer likes;
	private boolean liked; // 현재 사용자의 좋아요 상태
	private Integer viewCount; // 조회수
	private Integer commentCount; // 댓글 수 추가
	private String userId;
	private boolean isOwner; // 현재 사용자가 작성자인지 여부
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
