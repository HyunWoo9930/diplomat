package publicdata.hackathon.diplomats.domain.entity;

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
@Table(name = "public_diplomacy_programs")
@Data
@NoArgsConstructor
public class PublicDiplomacyProgram {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String countryName;        // 한글 국가명

	@Column(length = 200)
	private String countryEngName;     // 영문 국가명

	@Column(length = 2)
	private String countryIsoCode;     // ISO 2자리코드

	@Column(length = 400)
	private String businessName;       // 한글 사업명

	@Column(length = 400)
	private String businessEngName;    // 영문 사업명

	@Column(columnDefinition = "TEXT")
	private String businessPurpose;    // 사업 목적

	@Column(length = 100)
	private String businessTarget;     // 사업 대상

	@Column(length = 100)
	private String unitBusiness;       // 사업분류(단위사업)

	@Column(length = 100)
	private String detailBusiness;     // 사업분류(세부사업)

	private Integer businessYear;      // 사업연도

	@Column(length = 100)
	private String multiYearType;      // 사업유형

	@Column(nullable = false, length = 50)
	private String citizenType;        // 매칭된 시민력 유형

	private Integer matchScore;        // 매칭 점수

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
	public PublicDiplomacyProgram(String countryName, String countryEngName, String countryIsoCode,
		String businessName, String businessEngName, String businessPurpose,
		String businessTarget, String unitBusiness, String detailBusiness,
		Integer businessYear, String multiYearType, String citizenType, Integer matchScore) {
		this.countryName = countryName;
		this.countryEngName = countryEngName;
		this.countryIsoCode = countryIsoCode;
		this.businessName = businessName;
		this.businessEngName = businessEngName;
		this.businessPurpose = businessPurpose;
		this.businessTarget = businessTarget;
		this.unitBusiness = unitBusiness;
		this.detailBusiness = detailBusiness;
		this.businessYear = businessYear;
		this.multiYearType = multiYearType;
		this.citizenType = citizenType;
		this.matchScore = matchScore;
	}
}