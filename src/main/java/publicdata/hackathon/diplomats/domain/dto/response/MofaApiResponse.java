package publicdata.hackathon.diplomats.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MofaApiResponse {
	private Response response;

	@Data
	public static class Response {
		private Header header;
		private Body body;
	}

	@Data
	public static class Header {
		private String resultCode;
		private String resultMsg;
	}

	@Data
	public static class Body {
		private String dataType;
		private Items items;
		private int numOfRows;
		private int pageNo;
		private int totalCount;
	}

	@Data
	public static class Items {
		private List<Item> item;
	}

	@Data
	public static class Item {
		private String content;        // HTML 형태의 내용
		private String creator;        // 작성 부서

		@JsonProperty("file_url")
		private String fileUrl;        // 파일 URL

		private String title;          // 제목

		@JsonProperty("updt_date")
		private String updtDate;       // 업데이트 날짜 (yyyy-MM-dd 형태)
	}
}