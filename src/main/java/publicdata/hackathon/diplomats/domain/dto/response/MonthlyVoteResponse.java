package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import publicdata.hackathon.diplomats.domain.enums.VoteStatus;

@Data
@Builder
public class MonthlyVoteResponse {
	// 다이어리 투표 정보
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
	
	// ODA 투표 정보
	private Long odaVoteId;
	private String odaVoteTitle;
	private String odaVoteDescription;
	private VoteStatus odaVoteStatus;
	private LocalDateTime odaVoteStartDate;
	private LocalDateTime odaVoteEndDate;
	private Long odaTotalVotes;
	private List<OdaVoteCandidateResponse> odaCandidates;
	
	// 사용자 투표 정보
	private Boolean hasUserVoted;
	private Long userVotedCandidateId;
	private LocalDateTime userVotedAt;
	
	// 사용자 ODA 투표 정보
	private Boolean hasUserVotedOda;
	private Long userVotedOdaCandidateId;
	private LocalDateTime userVotedOdaAt;
}
