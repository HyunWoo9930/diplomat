package publicdata.hackathon.diplomats.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DiaryRequest extends BaseRequest {
	
	@NotBlank(message = "제목은 필수 입력값입니다.")
	@Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
	private String title;
	
	@NotBlank(message = "내용은 필수 입력값입니다.")
	@Size(max = 2000, message = "내용은 2000자 이하로 입력해주세요.")
	private String content;
	
	@NotBlank(message = "실천 행동은 필수 입력값입니다.")
	@Size(max = 200, message = "실천 행동은 200자 이하로 입력해주세요.")
	private String action;
}
