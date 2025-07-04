package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendedDiplomacyProgram {
	private String countryName;        // 국가명
	private String businessName;       // 사업명
	private String businessPurpose;    // 사업 목적
	private String businessTarget;     // 사업 대상
	private String unitBusiness;       // 사업분류
	private Integer businessYear;      // 사업연도
	private Integer matchScore;        // 매칭 점수
}