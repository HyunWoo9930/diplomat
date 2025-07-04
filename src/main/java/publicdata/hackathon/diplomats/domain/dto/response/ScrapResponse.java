package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScrapResponse {
	private boolean isScrapped;    // 현재 스크랩 상태
	private long scrapCount;       // 해당 뉴스의 총 스크랩 개수
	private String message;        // 결과 메시지
}