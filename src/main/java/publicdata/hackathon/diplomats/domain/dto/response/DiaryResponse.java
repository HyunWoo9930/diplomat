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
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String userId;
}
