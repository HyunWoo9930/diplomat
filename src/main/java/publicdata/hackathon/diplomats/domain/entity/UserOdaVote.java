package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOdaVote {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "oda_vote_id", nullable = false)
	private OdaVote odaVote;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "oda_vote_candidate_id", nullable = false)
	private OdaVoteCandidate odaVoteCandidate;

	@Column(nullable = false)
	private LocalDateTime votedAt;

	@PrePersist
	protected void onCreate() {
		this.votedAt = LocalDateTime.now();
	}
}
