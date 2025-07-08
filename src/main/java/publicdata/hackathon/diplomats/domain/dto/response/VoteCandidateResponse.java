package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoteCandidateResponse {
	private Long candidateId;
	private Long diaryId;
	private String diaryTitle;
	private String diaryDescription;
	private String diaryAction;
	private String authorName;
	private LocalDateTime diaryCreatedAt;
	private Integer diaryLikes;
	private Integer diaryViewCount;
	private Integer voteCount;
	private Integer ranking;
}
