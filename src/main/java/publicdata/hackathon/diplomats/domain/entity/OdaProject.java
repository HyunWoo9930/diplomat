package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OdaProject {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 500)
	private String title;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(length = 1000)
	private String url;

	@Column(nullable = false)
	private String category; // 환경, 교육, 보건, 여성, 기타

	@Column(nullable = false)
	private String countryName; // 대상 국가

	@Column
	private LocalDate projectStartDate;

	@Column
	private LocalDate projectEndDate;

	@Column
	private String budget; // 예산 정보

	@Column(nullable = false)
	private LocalDate publishDate;

	@Column(nullable = false)
	private Integer matchScore; // 분야 매칭 점수

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
