package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class UserVote {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "monthly_vote_id")
	private MonthlyVote monthlyVote;

	@ManyToOne
	@JoinColumn(name = "vote_candidate_id")
	private VoteCandidate voteCandidate;

	private LocalDateTime votedAt;

	@Builder
	public UserVote(User user, MonthlyVote monthlyVote, VoteCandidate voteCandidate) {
		this.user = user;
		this.monthlyVote = monthlyVote;
		this.voteCandidate = voteCandidate;
		this.votedAt = LocalDateTime.now();
	}
}
