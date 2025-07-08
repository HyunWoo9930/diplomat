package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserVoteResponse {
	private boolean hasVoted;
	private Long votedCandidateId;
	private String votedDiaryTitle;
	private LocalDateTime votedAt;
}
