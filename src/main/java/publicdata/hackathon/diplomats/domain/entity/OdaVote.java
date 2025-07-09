package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.enums.VoteStatus;

@Entity
@Data
@NoArgsConstructor
public class OdaVote {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Integer year;

	@Column(nullable = false)
	private Integer month;

	@Column(nullable = false)
	private String title;

	private String description;

	@Enumerated(EnumType.STRING)
	private VoteStatus status;

	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "odaVote", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OdaVoteCandidate> candidates = new ArrayList<>();

	@OneToMany(mappedBy = "odaVote", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserOdaVote> userOdaVotes = new ArrayList<>();

	@Builder
	public OdaVote(Integer year, Integer month, String title, String description, 
				   LocalDateTime startDate, LocalDateTime endDate) {
		this.year = year;
		this.month = month;
		this.title = title;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = VoteStatus.ACTIVE;
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
