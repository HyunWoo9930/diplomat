package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import publicdata.hackathon.diplomats.domain.enums.VoteStatus;

@Data
@Builder
public class VoteBannerResponse {
	private boolean hasActiveVote;
	private String title;
	private VoteStatus status;
	private LocalDateTime endDate;
	private Long totalVotes;
	private Integer totalCandidates;
	private String topCandidateTitle; // 현재 1위 후보
}
