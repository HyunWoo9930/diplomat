package publicdata.hackathon.diplomats.domain.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyPostsResponse {
	private List<MyPostItemResponse> posts;
	private long totalCount;
	private String filter; // ALL, FREE, DISCUSS, DIARY
}
