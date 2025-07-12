package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsItem {
	private Long id;
	private String title;
	private String summary;         // 요약 (150자 정도)
	private String url;
	private LocalDate publishDate;
	private String category;        // 필터 카테고리
	private String categoryDisplay; // 카테고리 한글명
	private Integer matchScore;     // 매칭 점수
	private boolean scrapped;       // 현재 사용자의 스크랩 상태
}
