package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiaryImageResponse {
	private Long id;
	private String originalFileName;
	private String base64Data;
	private String mimeType;
	private Integer imageOrder;
}
