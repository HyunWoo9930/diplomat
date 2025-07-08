package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import publicdata.hackathon.diplomats.domain.enums.DiscussType;

@Data
@Builder
public class PopularDiscussBoardResponse {
	private Long id;
	private String title;
	private String content;
	private DiscussType discussType;
	private Integer likes;
	private Integer viewCount;
	private String userId;
	private LocalDateTime createdAt;
	private Integer commentCount;
}
