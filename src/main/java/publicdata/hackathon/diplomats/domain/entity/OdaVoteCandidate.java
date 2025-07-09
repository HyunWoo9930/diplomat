package publicdata.hackathon.diplomats.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OdaVoteCandidate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "oda_vote_id", nullable = false)
	private OdaVote odaVote;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "oda_project_id", nullable = false)
	private OdaProject odaProject;

	@Column(nullable = false)
	private Integer voteCount = 0;

	public void incrementVoteCount() {
		this.voteCount++;
	}

	public void decrementVoteCount() {
		if (this.voteCount > 0) {
			this.voteCount--;
		}
	}
}
