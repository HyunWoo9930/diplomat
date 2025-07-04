package publicdata.hackathon.diplomats.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PublicDiplomacyApiResponse {
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
		private int currentCount;
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
		@JsonProperty("country_nm")
		private String countryName;          // 한글 국가명

		@JsonProperty("country_eng_nm")
		private String countryEngName;       // 영문 국가명

		@JsonProperty("country_iso_alp2")
		private String countryIsoCode;       // ISO 2자리코드

		@JsonProperty("kor_business_nm")
		private String businessName;         // 한글 사업명

		@JsonProperty("eng_business_nm")
		private String businessEngName;      // 영문 사업명

		@JsonProperty("business_purpose")
		private String businessPurpose;      // 사업 목적

		@JsonProperty("business_target")
		private String businessTarget;       // 사업 대상

		@JsonProperty("unit_business")
		private String unitBusiness;         // 사업분류(단위사업)

		@JsonProperty("detail_business")
		private String detailBusiness;       // 사업분류(세부사업)

		@JsonProperty("business_year")
		private Integer businessYear;        // 사업연도

		@JsonProperty("multi_year_type")
		private String multiYearType;        // 사업유형
	}
}