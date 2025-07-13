package publicdata.hackathon.diplomats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.request.VoteRequest;
import publicdata.hackathon.diplomats.domain.dto.response.ApiResponse;
import publicdata.hackathon.diplomats.domain.dto.response.MonthlyVoteResponse;
import publicdata.hackathon.diplomats.domain.dto.response.UserVoteResponse;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.MonthlyVoteService;
import publicdata.hackathon.diplomats.utils.SecurityUtils;

@Slf4j
@RestController
@RequestMapping("/api/v1/monthly-vote")
@RequiredArgsConstructor
@Tag(name = "🗳️ 투표", description = "월별 외교실천일지 투표 관련 API")
@CrossOrigin(origins = "*")
public class MonthlyVoteController {

	private final MonthlyVoteService monthlyVoteService;

	@PostMapping("/create")
	@Operation(summary = "월별 투표 생성", description = "이번 달 인기 일지 상위 10개로 투표를 생성합니다.")
	public ResponseEntity<ApiResponse<String>> createMonthlyVote() {
		log.info("월별 투표 생성 요청");
		
		String result = monthlyVoteService.createMonthlyVote();
		return ResponseEntity.ok(ApiResponse.success(result));
	}

	@GetMapping("/current")
	@Operation(summary = "현재 투표 조회", description = "현재 진행중인 투표 정보와 후보 목록, 사용자 투표 정보를 조회합니다.")
	public ResponseEntity<ApiResponse<MonthlyVoteResponse>> getCurrentVote(Authentication authentication) {
		log.info("현재 투표 조회 요청");
		
		String username = null;
		if (authentication != null && authentication.isAuthenticated() && 
			!"anonymousUser".equals(authentication.getPrincipal())) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			username = userDetails.getUsername();
		}
		
		MonthlyVoteResponse response = monthlyVoteService.getCurrentVote(username);
		if (response == null) {
			return ResponseEntity.ok(ApiResponse.success("현재 진행중인 투표가 없습니다.", null));
		}
		return ResponseEntity.ok(ApiResponse.success("현재 투표 정보를 조회했습니다.", response));
	}

	@PostMapping("/vote")
	@Operation(summary = "투표 참여", description = "선택한 후보에게 투표합니다.")
	public ResponseEntity<ApiResponse<String>> vote(@Valid @RequestBody VoteRequest voteRequest) {
		String currentUserId = SecurityUtils.getCurrentUserIdString();
		log.info("투표 참여 요청: userId={}, candidateId={}", currentUserId, voteRequest.getCandidateId());
		
		String result = monthlyVoteService.vote(currentUserId, voteRequest);
		return ResponseEntity.ok(ApiResponse.success(result));
	}

	@GetMapping("/my-vote")
	@Operation(summary = "내 투표 내역 조회", description = "현재 투표에서 내가 투표한 내역을 조회합니다.")
	public ResponseEntity<ApiResponse<UserVoteResponse>> getMyVote() {
		String currentUserId = SecurityUtils.getCurrentUserIdString();
		log.info("내 투표 내역 조회: userId={}", currentUserId);
		
		UserVoteResponse response = monthlyVoteService.getMyVote(currentUserId);
		return ResponseEntity.ok(ApiResponse.success("투표 내역을 조회했습니다.", response));
	}

	@GetMapping("/result")
	@Operation(summary = "투표 결과 조회", description = "현재 투표의 실시간 결과를 조회합니다.")
	public ResponseEntity<ApiResponse<MonthlyVoteResponse>> getVoteResult() {
		log.info("투표 결과 조회 요청");
		
		MonthlyVoteResponse response = monthlyVoteService.getVoteResult();
		return ResponseEntity.ok(ApiResponse.success("투표 결과를 조회했습니다.", response));
	}

	@GetMapping("/result/{year}/{month}")
	@Operation(
		summary = "특정 월 투표 결과 조회", 
		description = "지정된 년도와 월의 투표 결과를 조회합니다.",
		parameters = {
			@io.swagger.v3.oas.annotations.Parameter(name = "year", description = "조회할 년도 (예: 2025)", example = "2025"),
			@io.swagger.v3.oas.annotations.Parameter(name = "month", description = "조회할 월 (1-12)", example = "7")
		}
	)
	public ResponseEntity<ApiResponse<MonthlyVoteResponse>> getVoteResultByMonth(
		@org.springframework.web.bind.annotation.PathVariable Integer year,
		@org.springframework.web.bind.annotation.PathVariable Integer month) {
		
		log.info("특정 월 투표 결과 조회 요청: year={}, month={}", year, month);
		
		// 입력값 검증
		if (year < 2020 || year > 2030) {
			return ResponseEntity.badRequest().body(
				ApiResponse.fail("유효하지 않은 년도입니다. (2020-2030)")
			);
		}
		
		if (month < 1 || month > 12) {
			return ResponseEntity.badRequest().body(
				ApiResponse.fail("유효하지 않은 월입니다. (1-12)")
			);
		}
		
		MonthlyVoteResponse response = monthlyVoteService.getVoteResultByMonth(year, month);
		return ResponseEntity.ok(ApiResponse.success(
			year + "년 " + month + "월 투표 결과를 조회했습니다.", response));
	}
}
