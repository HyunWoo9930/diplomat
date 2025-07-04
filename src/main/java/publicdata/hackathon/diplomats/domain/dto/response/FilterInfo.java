package publicdata.hackathon.diplomats.domain.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilterInfo {
	private String currentFilter;
	private String currentFilterDisplay;
	private List<FilterOption> availableFilters;
}
