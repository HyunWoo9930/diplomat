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
public class FreeBoardResponse {
	private Long id;
	private String title;
	private String content;
	private int likes;
	private int viewCount;
	private String userId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public FreeBoardResponse(Long id, String title, String content, int likes, int viewCount, String userId, LocalDateTime createdAt,
		LocalDateTime updatedAt) {
		this.id = id;
		this.title = title;
		this.content = content;
		this.likes = likes;
		this.viewCount = viewCount;
		this.userId = userId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
