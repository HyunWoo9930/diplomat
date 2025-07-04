package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "press_releases")
@Data
@NoArgsConstructor
public class PressRelease {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(nullable = false, length = 500)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(length = 1000)
	private String url;

	private LocalDate publishDate;

	@Column(nullable = false, length = 50)
	private String citizenType;

	private Integer matchScore;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	@Builder
	public PressRelease(String title, String content, String url, LocalDate publishDate,
		String citizenType, Integer matchScore) {
		this.title = title;
		this.content = content;
		this.url = url;
		this.publishDate = publishDate;
		this.citizenType = citizenType;
		this.matchScore = matchScore;
	}
}