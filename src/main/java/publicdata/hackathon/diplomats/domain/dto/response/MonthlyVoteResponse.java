package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import publicdata.hackathon.diplomats.domain.enums.VoteStatus;

@Data
@Builder
public class MonthlyVoteResponse {
	private Long id;
	private Integer year;
	private Integer month;
	private String title;
	private String description;
	private VoteStatus status;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private Long totalVotes;
	private List<VoteCandidateResponse> candidates;
}
