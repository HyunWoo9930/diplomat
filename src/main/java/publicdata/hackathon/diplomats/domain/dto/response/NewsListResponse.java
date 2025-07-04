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
}

