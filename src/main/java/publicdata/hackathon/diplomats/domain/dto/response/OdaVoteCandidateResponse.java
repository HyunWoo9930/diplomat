package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OdaVoteCandidateResponse {
	private Long id;
	private OdaProjectResponse odaProject;
	private Integer voteCount;
	private Double votePercentage;
	private Integer rank;
}
