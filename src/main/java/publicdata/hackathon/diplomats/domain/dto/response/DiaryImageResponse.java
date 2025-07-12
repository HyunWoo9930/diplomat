package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiaryImageResponse {
	private Long id;
	private String originalFileName;
	private String imageUrl;    // 이미지 URL (Base64 대신)
	private String mimeType;
	private Integer imageOrder;
	private Long fileSize;      // 파일 크기 (바이트)
}
