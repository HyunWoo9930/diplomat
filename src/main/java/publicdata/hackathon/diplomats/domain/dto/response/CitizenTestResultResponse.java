package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CitizenTestResultResponse {
	private String resultType;        // "CLIMATE_ACTION" 등
	private String displayName;       // "기후행동형"
	private String description;       // 유형 설명
	private String message;          // 결과 메시지
}