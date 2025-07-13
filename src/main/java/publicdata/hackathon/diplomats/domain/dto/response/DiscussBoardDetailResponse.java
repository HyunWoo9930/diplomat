package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import publicdata.hackathon.diplomats.domain.enums.DiscussType;

@Data
@Builder
public class DiscussBoardDetailResponse {
	private String title;
	private String content;
	private DiscussType discussType;
	private String discussTypeDisplay;
	private Integer likes;
	private Integer viewCount;
	private String userId;
	private boolean owner; // 현재 사용자가 작성자인지 여부
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<DiscussBoardCommentResponse> discussBoardComments;
	private List<DiscussBoardImageResponse> discussBoardImages;
}
