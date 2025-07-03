package publicdata.hackathon.diplomats.domain.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
	private String userId;
	private String password;
}
