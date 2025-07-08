package publicdata.hackathon.diplomats.domain.dto.request;

import lombok.Data;
import publicdata.hackathon.diplomats.domain.enums.DiscussType;

@Data
public class DiscussBoardUpdateRequest {
	private String title;
	private String content;
	private DiscussType discussType;
}
