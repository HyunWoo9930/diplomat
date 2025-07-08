package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import publicdata.hackathon.diplomats.domain.enums.DiscussType;
import publicdata.hackathon.diplomats.domain.enums.PostType;

@Data
@Builder
public class MyPostItemResponse {
	private Long id;
	private String title;
	private String content;
	private PostType postType; // FREE_BOARD, DISCUSS_BOARD, DIARY
	private DiscussType discussType; // 토론게시판인 경우만
	private String action; // 일지인 경우만
	private Integer likes;
	private Integer viewCount;
	private Integer commentCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
