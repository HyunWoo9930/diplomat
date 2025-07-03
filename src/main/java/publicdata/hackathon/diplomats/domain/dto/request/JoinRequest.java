package publicdata.hackathon.diplomats.domain.dto.request;

import lombok.Data;

@Data
public class JoinRequest {
	private String userId;
	private String password;
	private String name;
}
