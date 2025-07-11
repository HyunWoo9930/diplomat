package publicdata.hackathon.diplomats.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest extends BaseRequest {
	
	@NotNull(message = "투표할 후보를 선택해주세요.")
	private Long candidateId;
}
