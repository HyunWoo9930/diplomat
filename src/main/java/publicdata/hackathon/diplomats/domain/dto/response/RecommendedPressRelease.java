package publicdata.hackathon.diplomats.domain.dto.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendedPressRelease {
	private String title;
	private String url;
	private LocalDate publishDate;
	private String summary;
	private Integer matchScore;
}