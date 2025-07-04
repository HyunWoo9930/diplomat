package publicdata.hackathon.diplomats.domain.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class SubmitAnswersRequest {
	private List<AnswerDto> answers;
}