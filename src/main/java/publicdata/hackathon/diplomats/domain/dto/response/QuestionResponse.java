package publicdata.hackathon.diplomats.domain.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionResponse {
	private Long id;
	private String content;
	private Integer questionOrder;
	private List<QuestionOptionResponse> options;
}