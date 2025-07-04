package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilterOption {
	private String value;
	private String display;
	private long count; // 해당 필터의 뉴스 개수
}
