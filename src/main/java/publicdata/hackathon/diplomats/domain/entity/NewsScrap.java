package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "news_scraps", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"user_id", "press_release_id"})
})
@Data
@NoArgsConstructor
public class NewsScrap {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "press_release_id", nullable = false)
	private PressRelease pressRelease;

	private LocalDateTime scrapedAt;

	@PrePersist
	protected void onCreate() {
		scrapedAt = LocalDateTime.now();
	}

	@Builder
	public NewsScrap(User user, PressRelease pressRelease) {
		this.user = user;
		this.pressRelease = pressRelease;
	}
}