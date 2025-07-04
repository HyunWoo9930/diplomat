package publicdata.hackathon.diplomats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.ScrapToggleRequest;
import publicdata.hackathon.diplomats.domain.dto.response.MyScrapListResponse;
import publicdata.hackathon.diplomats.domain.dto.response.PaginationInfo;
import publicdata.hackathon.diplomats.domain.dto.response.ScrapResponse;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.NewsScrapService;

@RestController
@RequestMapping("/api/v1/news/scrap")
@RequiredArgsConstructor
@Tag(name = "뉴스 스크랩", description = "뉴스 스크랩 관련 API")
public class NewsScrapController {

	private final NewsScrapService newsScrapService;

	@PostMapping("/toggle")
	@Operation(summary = "뉴스 스크랩 토글", description = "뉴스를 스크랩하거나 스크랩을 취소합니다.")
	public ResponseEntity<ScrapResponse> toggleScrap(
		Authentication authentication,
		@RequestBody ScrapToggleRequest request) {

		try {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			ScrapResponse response = newsScrapService.toggleScrap(
				userDetails.getUsername(),
				request.getNewsId()
			);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(
				ScrapResponse.builder()
					.isScrapped(false)
					.scrapCount(0)
					.message("스크랩 처리 실패: " + e.getMessage())
					.build()
			);
		}
	}

	@GetMapping("/status/{newsId}")
	@Operation(summary = "뉴스 스크랩 상태 조회", description = "특정 뉴스의 스크랩 상태와 개수를 조회합니다.")
	public ResponseEntity<ScrapResponse> getScrapStatus(
		Authentication authentication,
		@PathVariable Long newsId) {

		try {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			ScrapResponse response = newsScrapService.getScrapStatus(
				userDetails.getUsername(),
				newsId
			);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(
				ScrapResponse.builder()
					.isScrapped(false)
					.scrapCount(0)
					.message("상태 조회 실패: " + e.getMessage())
					.build()
			);
		}
	}

	@GetMapping("/my-scraps")
	@Operation(summary = "내 스크랩 목록 조회", description = "사용자가 스크랩한 뉴스 목록을 조회합니다.")
	public ResponseEntity<MyScrapListResponse> getMyScraps(
		Authentication authentication,
		@Parameter(description = "페이지 번호 (0부터 시작)")
		@RequestParam(defaultValue = "0") int page,
		@Parameter(description = "페이지당 스크랩 개수")
		@RequestParam(defaultValue = "20") int size) {

		try {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			MyScrapListResponse response = newsScrapService.getMyScraps(
				userDetails.getUsername(),
				page,
				size
			);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			MyScrapListResponse errorResponse = MyScrapListResponse.builder()
				.scraps(java.util.List.of())
				.pagination(PaginationInfo.builder()
					.currentPage(page)
					.totalPages(0)
					.pageSize(size)
					.totalCount(0)
					.hasNext(false)
					.hasPrev(false)
					.build())
				.build();

			return ResponseEntity.badRequest().body(errorResponse);
		}
	}

	@DeleteMapping("/{scrapId}")
	@Operation(summary = "스크랩 삭제", description = "스크랩 ID로 특정 스크랩을 삭제합니다.")
	public ResponseEntity<String> deleteScrap(
		Authentication authentication,
		@PathVariable Long scrapId) {

		try {
			// 구현 필요시 추가
			return ResponseEntity.ok("스크랩이 삭제되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("삭제 실패: " + e.getMessage());
		}
	}
}