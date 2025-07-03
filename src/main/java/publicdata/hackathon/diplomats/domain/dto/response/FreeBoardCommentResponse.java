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
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String userId;

	@Builder
	public FreeBoardCommentResponse(String content, LocalDateTime createdAt, LocalDateTime updatedAt, String userId) {
		this.content = content;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.userId = userId;
	}
}
