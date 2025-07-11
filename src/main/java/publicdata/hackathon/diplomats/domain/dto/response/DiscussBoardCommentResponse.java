package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import publicdata.hackathon.diplomats.domain.enums.DiscussCommentType;

@Data
@Builder
public class DiscussBoardCommentResponse {
	private Long id;
	private String content;
	private String userId;
	private boolean isOwner; // 현재 사용자가 작성자인지 여부
	private DiscussCommentType commentType;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
