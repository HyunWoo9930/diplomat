package publicdata.hackathon.diplomats.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.enums.UserLevel;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String userId;
	
	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String name;

	@Column
	private String citizenType;

	@Column(nullable = false)
	@Builder.Default
	private Integer totalStamps = 0; // 총 획득 스탬프 수

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private UserLevel currentLevel = UserLevel.LEVEL_1; // 현재 레벨

	/**
	 * 스탬프 추가 및 레벨 업데이트
	 */
	public UserLevel addStamps(int stampCount) {
		UserLevel previousLevel = this.currentLevel;
		this.totalStamps += stampCount;
		this.currentLevel = UserLevel.fromStampCount(this.totalStamps);
		return previousLevel;
	}

	/**
	 * 다음 레벨까지 필요한 스탬프 수
	 */
	public int getStampsToNextLevel() {
		return this.currentLevel.getStampsToNextLevel(this.totalStamps);
	}

	/**
	 * 레벨업 여부 확인
	 */
	public boolean hasLeveledUp(UserLevel previousLevel) {
		return !this.currentLevel.equals(previousLevel);
	}
}
