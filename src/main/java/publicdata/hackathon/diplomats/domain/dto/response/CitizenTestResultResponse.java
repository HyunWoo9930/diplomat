package publicdata.hackathon.diplomats.domain.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CitizenTestResultResponse {
	private String resultType;
	private String displayName;
	private String description;
	private List<RecommendedPressRelease> recommendedNews;
	private List<RecommendedDiplomacyProgram> recommendedPrograms;
	private String message;
}