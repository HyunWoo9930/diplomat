package publicdata.hackathon.diplomats.domain.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommunityResponse {
	private List<PopularFreeBoardResponse> popularFreeBoards;
	private List<PopularDiscussBoardResponse> popularDiscussBoards;
}
