package publicdata.hackathon.diplomats.domain.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OdaApiResponse {
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
		private Items items;
		private String numOfRows;
		private String pageNo;
		private String totalCount;
	}

	@Data
	public static class Items {
		private List<Item> item;
	}

	@Data
	public static class Item {
		@JsonProperty("title")
		private String title;

		@JsonProperty("content")
		private String content;

		@JsonProperty("fileUrl")
		private String fileUrl;

		@JsonProperty("updtDate")
		private String updtDate;

		@JsonProperty("country")
		private String country;

		@JsonProperty("category")
		private String category;

		@JsonProperty("budget")
		private String budget;

		@JsonProperty("startDate")
		private String startDate;

		@JsonProperty("endDate")
		private String endDate;

		// 기본 필드들 (실제 API 응답에 맞게 조정 필요)
		@JsonProperty("projectTitle")
		private String projectTitle;

		@JsonProperty("implementingAgency")
		private String implementingAgency;

		@JsonProperty("projectDescription")
		private String projectDescription;

		@JsonProperty("targetCountry")
		private String targetCountry;

		@JsonProperty("sector")
		private String sector;

		@JsonProperty("projectBudget")
		private String projectBudget;

		@JsonProperty("projectStartDate")
		private String projectStartDate;

		@JsonProperty("projectEndDate")
		private String projectEndDate;
	}
}
