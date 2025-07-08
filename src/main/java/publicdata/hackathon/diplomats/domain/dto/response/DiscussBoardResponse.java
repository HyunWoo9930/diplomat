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
	private Integer likes;
	private Integer viewCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String userId;
}
