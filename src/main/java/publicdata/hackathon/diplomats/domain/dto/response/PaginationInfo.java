package publicdata.hackathon.diplomats.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationInfo {
	private int currentPage;
	private int totalPages;
	private int pageSize;
	private long totalCount;
	private boolean hasNext;
	private boolean hasPrev;
}
