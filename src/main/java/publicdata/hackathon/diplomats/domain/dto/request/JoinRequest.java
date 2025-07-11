package publicdata.hackathon.diplomats.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoinRequest extends BaseRequest {
	
	@NotBlank(message = "아이디는 필수 입력값입니다.")
	@Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해주세요.")
	@Pattern(regexp = "^[a-zA-Z0-9]*$", message = "아이디는 영문자와 숫자만 사용 가능합니다.")
	private String userId;
	
	@NotBlank(message = "비밀번호는 필수 입력값입니다.")
	@Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하로 입력해주세요.")
	private String password;
}
