package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiaryCommentResponse {
	private Long id;
	private String content;
	private String userId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
