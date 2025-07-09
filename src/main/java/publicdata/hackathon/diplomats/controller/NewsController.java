package publicdata.hackathon.diplomats.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.response.FilterInfo;
import publicdata.hackathon.diplomats.domain.dto.response.NewsListResponse;
import publicdata.hackathon.diplomats.domain.dto.response.PaginationInfo;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.NewsService;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Tag(name = "📰 뉴스/보도자료", description = "외교부 뉴스 조회 API")
public class NewsController {

	private final NewsService newsService;

	@GetMapping
	@Operation(
		summary = "외교 뉴스 조회",
		description = """
			외교부 보도자료를 조회합니다.
			
			필터 옵션:
			- ALL: 전체 뉴스
			- ESG: ESG 관련 뉴스
			- CLIMATE: 기후 관련 뉴스  
			- CULTURE: 문화 관련 뉴스
			- ODA: 개발협력(ODA) 관련 뉴스
			
			페이지네이션:
			- page: 페이지 번호 (0부터 시작)
			- size: 페이지당 뉴스 개수 (기본 20개)
			"""
	)
	public ResponseEntity<NewsListResponse> getNews(
		@Parameter(description = "필터 (ALL, ESG, CLIMATE, CULTURE, ODA)")
		@RequestParam(defaultValue = "ALL") String filter,
		@Parameter(description = "페이지 번호 (0부터 시작)")
		@RequestParam(defaultValue = "0") int page,
		@Parameter(description = "페이지당 뉴스 개수")
		@RequestParam(defaultValue = "20") int size
	) {
		try {
			NewsListResponse response = newsService.getNews(filter, page, size);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			NewsListResponse errorResponse = NewsListResponse.builder()
				.news(List.of())
				.pagination(PaginationInfo.builder()
					.currentPage(page)
					.totalPages(0)
					.pageSize(size)
					.totalCount(0)
					.hasNext(false)
					.hasPrev(false)
					.build())
				.filter(FilterInfo.builder()
					.currentFilter("ALL")
					.currentFilterDisplay("전체")
					.availableFilters(List.of())
					.build())
				.build();

			return ResponseEntity.badRequest().body(errorResponse);
		}
	}

	@GetMapping("/personalized")
	@Operation(
		summary = "맞춤형 외교 뉴스 조회",
		description = """
			사용자의 시민력 유형에 맞는 맞춤형 외교 뉴스를 조회합니다.
			
			- 사용자가 시민력 테스트를 완료한 경우: 해당 유형에 맞는 뉴스 제공
			- 테스트를 완료하지 않은 경우: 전체 뉴스 제공
			
			페이지네이션:
			- page: 페이지 번호 (0부터 시작)
			- size: 페이지당 뉴스 개수 (기본 20개)
			"""
	)
	public ResponseEntity<NewsListResponse> getPersonalizedNews(
		Authentication authentication,
		@Parameter(description = "페이지 번호 (0부터 시작)")
		@RequestParam(defaultValue = "0") int page,
		@Parameter(description = "페이지당 뉴스 개수")
		@RequestParam(defaultValue = "20") int size
	) {
		try {
			CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
			NewsListResponse response = newsService.getPersonalizedNews(
				userDetails.getUsername(),
				page,
				size
			);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			// 에러 시 빈 응답 반환
			NewsListResponse errorResponse = NewsListResponse.builder()
				.news(List.of())
				.pagination(PaginationInfo.builder()
					.currentPage(page)
					.totalPages(0)
					.pageSize(size)
					.totalCount(0)
					.hasNext(false)
					.hasPrev(false)
					.build())
				.filter(FilterInfo.builder()
					.currentFilter("PERSONALIZED")
					.currentFilterDisplay("맞춤형")
					.availableFilters(List.of())
					.build())
				.build();

			return ResponseEntity.badRequest().body(errorResponse);
		}
	}
}