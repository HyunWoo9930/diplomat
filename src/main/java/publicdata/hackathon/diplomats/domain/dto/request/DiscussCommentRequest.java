package publicdata.hackathon.diplomats.domain.dto.request;

import lombok.Data;
import publicdata.hackathon.diplomats.domain.enums.DiscussCommentType;

@Data
public class DiscussCommentRequest {
	private String comment;
	private DiscussCommentType commentType;
}
