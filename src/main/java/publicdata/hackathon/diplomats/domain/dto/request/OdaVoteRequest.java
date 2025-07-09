package publicdata.hackathon.diplomats.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ODA 투표 요청")
public class OdaVoteRequest {
	
	@Schema(description = "투표할 후보(ODA 프로젝트) ID", example = "3", required = true)
	private Long candidateId;
}
