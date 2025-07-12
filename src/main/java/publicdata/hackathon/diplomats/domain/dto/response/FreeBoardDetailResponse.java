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
	private String title;
	private String content;
	private int likes;
	private boolean liked; // 현재 사용자의 좋아요 상태
	private int viewCount;
	private String userId;
	private boolean isOwner; // 현재 사용자가 작성자인지 여부
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public FreeBoardDetailResponse(List<FreeBoardCommentResponse> freeBoardComments, List<FreeBoardImageResponse> freeBoardImages, 
			String title, String content, int likes, boolean liked, int viewCount, String userId, boolean isOwner,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.freeBoardComments = freeBoardComments;
		this.freeBoardImages = freeBoardImages;
		this.title = title;
		this.content = content;
		this.likes = likes;
		this.liked = liked;
		this.viewCount = viewCount;
		this.userId = userId;
		this.isOwner = isOwner;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
