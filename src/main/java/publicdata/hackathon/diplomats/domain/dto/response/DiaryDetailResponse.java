package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiaryDetailResponse {
	private String title;
	private String description;
	private String action;
	private Integer likes;
	private boolean liked; // 현재 사용자의 좋아요 상태
	private String userId;
	private boolean isOwner; // 현재 사용자가 작성자인지 여부
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<DiaryCommentResponse> diaryComments;
	private List<DiaryImageResponse> diaryImages;
}
