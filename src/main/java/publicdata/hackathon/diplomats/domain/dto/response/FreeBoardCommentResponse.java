package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.entity.User;

@Data
@NoArgsConstructor
public class FreeBoardCommentResponse {
	private Long id; // 댓글 ID 추가
	private String content;
	private String userId;
	private boolean isOwner; // 현재 사용자가 작성자인지 여부
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public FreeBoardCommentResponse(Long id, String content, String userId, boolean isOwner, 
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.id = id;
		this.content = content;
		this.userId = userId;
		this.isOwner = isOwner;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
