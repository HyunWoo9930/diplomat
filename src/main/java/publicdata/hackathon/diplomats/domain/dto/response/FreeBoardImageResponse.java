package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FreeBoardImageResponse {
	private Long id;
	private String originalFileName;
	private String base64Data; // Base64 인코딩된 이미지 데이터
	private String mimeType;   // image/jpeg, image/png 등
	private Integer imageOrder;
}