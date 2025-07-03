package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.entity.FreeBoardComment;

@Data
@NoArgsConstructor
public class FreeBoardDetailResponse {
	List<FreeBoardCommentResponse> freeBoardComments;
	List<FreeBoardImageResponse>  freeBoardImages;
	private LocalDateTime updatedAt;
	private LocalDateTime createdAt;
	private String userId;
	private String title;
	private String content;
	private int likes;
	private int viewCount;

	@Builder
	public FreeBoardDetailResponse(List<FreeBoardCommentResponse> freeBoardComments, List<FreeBoardImageResponse> freeBoardImages, LocalDateTime updatedAt,
		LocalDateTime createdAt, String userId, String title, String content, int likes, int viewCount) {
		this.freeBoardComments = freeBoardComments;
		this.freeBoardImages = freeBoardImages;
		this.updatedAt = updatedAt;
		this.createdAt = createdAt;
		this.userId = userId;
		this.title = title;
		this.content = content;
		this.likes = likes;
		this.viewCount = viewCount;
	}
}
