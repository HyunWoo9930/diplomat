package publicdata.hackathon.diplomats.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoinRequest extends BaseRequest {
	
	@NotBlank(message = "아이디는 필수 입력값입니다.")
	private String userId;
	
	@NotBlank(message = "비밀번호는 필수 입력값입니다.")
	private String password;
}
