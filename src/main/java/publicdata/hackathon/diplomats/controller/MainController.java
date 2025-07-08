package publicdata.hackathon.diplomats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.response.VoteBannerResponse;
import publicdata.hackathon.diplomats.service.DiaryService;
import publicdata.hackathon.diplomats.service.MonthlyVoteService;

@RestController
@RequestMapping("/api/v1/main")
@RequiredArgsConstructor
@Tag(name = "메인페이지", description = "메인페이지 관련 API")
public class MainController {

	private final MonthlyVoteService monthlyVoteService;
	private final DiaryService diaryService;

	@GetMapping("/vote-banner")
	@Operation(summary = "투표 배너 정보", description = "메인페이지에 표시할 투표 배너 정보를 조회합니다.")
	public ResponseEntity<VoteBannerResponse> getVoteBanner() {
		VoteBannerResponse response = monthlyVoteService.getVoteBanner();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/top-diaries")
	@Operation(summary = "이번 달 인기 일지", description = "이번 달 인기 일지 상위 10개를 조회합니다.")
	public ResponseEntity<?> getTopMonthlyDiaries() {
		return ResponseEntity.ok(diaryService.getTopMonthlyDiaries());
	}
}
