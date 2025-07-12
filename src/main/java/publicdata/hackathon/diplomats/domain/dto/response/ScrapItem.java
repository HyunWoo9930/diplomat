package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScrapItem {
	private Long scrapId;
	private Long newsId;
	private String title;
	private String summary;
	private String url;
	private LocalDate publishDate;
	private LocalDateTime scrapedAt;
	private String category;
	private String categoryDisplay;
	private boolean scrapped; // 스크랩된 아이템이므로 항상 true
}
