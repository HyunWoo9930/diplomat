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
	private String userId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<DiaryCommentResponse> diaryComments;
	private List<DiaryImageResponse> diaryImages;
}
