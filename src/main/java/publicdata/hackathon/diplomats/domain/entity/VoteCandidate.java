package publicdata.hackathon.diplomats.domain.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class VoteCandidate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "monthly_vote_id")
	private MonthlyVote monthlyVote;

	@ManyToOne
	@JoinColumn(name = "diary_id")
	private Diary diary;

	private Integer voteCount = 0;
	private Integer ranking;

	@OneToMany(mappedBy = "voteCandidate", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserVote> userVotes = new ArrayList<>();

	@Builder
	public VoteCandidate(MonthlyVote monthlyVote, Diary diary, Integer ranking) {
		this.monthlyVote = monthlyVote;
		this.diary = diary;
		this.ranking = ranking;
		this.voteCount = 0;
	}

	public void addVote() {
		this.voteCount++;
	}

	public void removeVote() {
		if (this.voteCount > 0) {
			this.voteCount--;
		}
	}
}
