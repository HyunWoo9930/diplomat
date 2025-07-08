package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PopularFreeBoardResponse {
	private Long id;
	private String title;
	private String content;
	private Integer likes;
	private Integer viewCount;
	private String userId;
	private LocalDateTime createdAt;
	private Integer commentCount;
}
