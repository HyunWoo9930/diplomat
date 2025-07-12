package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsListResponse {
	private List<NewsItem> news;
	private PaginationInfo pagination;
	private FilterInfo filter;
	private String citizenType;        // 사용자의 시민력 유형 (예: CLIMATE_ACTION)
	private String citizenTypeDisplay; // 시민력 유형 한글명 (예: 기후행동형)
}

