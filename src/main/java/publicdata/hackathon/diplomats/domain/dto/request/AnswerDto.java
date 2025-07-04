package publicdata.hackathon.diplomats.domain.dto.request;

import lombok.Data;

@Data
public class AnswerDto {
	private Long questionId;
	private Long optionId;
}