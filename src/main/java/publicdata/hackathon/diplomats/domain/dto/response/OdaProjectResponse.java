package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "ODA 프로젝트 정보")
public class OdaProjectResponse {
	
	@Schema(description = "프로젝트 ID", example = "1")
	private Long id;
	
	@Schema(description = "프로젝트 제목", example = "베트남 기후변화 대응 역량 강화 사업")
	private String title;
	
	@Schema(description = "프로젝트 상세 내용")
	private String content;
	
	@Schema(description = "프로젝트 관련 URL")
	private String url;
	
	@Schema(description = "분야 구분", example = "ENVIRONMENT", 
			allowableValues = {"ENVIRONMENT", "EDUCATION", "HEALTH", "WOMEN", "OTHERS"})
	private String category;
	
	@Schema(description = "대상 국가", example = "베트남")
	private String countryName;
	
	@Schema(description = "프로젝트 시작일")
	private LocalDate projectStartDate;
	
	@Schema(description = "프로젝트 종료일")
	private LocalDate projectEndDate;
	
	@Schema(description = "예산 정보", example = "100억원")
	private String budget;
	
	@Schema(description = "발표일")
	private LocalDate publishDate;
	
	@Schema(description = "분야 매칭 점수 (높을수록 해당 분야와 관련성이 높음)", example = "85")
	private Integer matchScore;
	
	@Schema(description = "프로젝트 요약 (150자 이내)")
	private String summary;
}
