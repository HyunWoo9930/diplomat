package publicdata.hackathon.diplomats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.VoteRequest;
import publicdata.hackathon.diplomats.domain.dto.response.MonthlyVoteResponse;
import publicdata.hackathon.diplomats.domain.dto.response.UserVoteResponse;
import publicdata.hackathon.diplomats.domain.dto.response.VoteBannerResponse;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.MonthlyVoteService;

@RestController
@RequestMapping("/api/v1/monthly-vote")
@RequiredArgsConstructor
@Tag(name = "🗳️ 투표", description = "월별 외교실천일지 투표 관련 API")
public class MonthlyVoteController {

	private final MonthlyVoteService monthlyVoteService;

	@PostMapping("/create")
	@Operation(summary = "월별 투표 생성", description = "이번 달 인기 일지 상위 10개로 투표를 생성합니다.")
	public ResponseEntity<String> createMonthlyVote() {
		try {
			String result = monthlyVoteService.createMonthlyVote();
			return ResponseEntity.ok(result);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/current")
	@Operation(summary = "현재 투표 조회", description = "현재 진행중인 투표 정보와 후보 목록을 조회합니다.")
	public ResponseEntity<MonthlyVoteResponse> getCurrentVote() {
		MonthlyVoteResponse response = monthlyVoteService.getCurrentVote();
		if (response == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(response);
	}

	@PostMapping("/vote")
	@Operation(summary = "투표 참여", description = "선택한 후보에게 투표합니다.")
	public ResponseEntity<String> vote(Authentication authentication, @RequestBody VoteRequest voteRequest) {
		try {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String result = monthlyVoteService.vote(userDetails.getUsername(), voteRequest);
			return ResponseEntity.ok(result);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/my-vote")
	@Operation(summary = "내 투표 내역 조회", description = "현재 투표에서 내가 투표한 내역을 조회합니다.")
	public ResponseEntity<UserVoteResponse> getMyVote(Authentication authentication) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		UserVoteResponse response = monthlyVoteService.getMyVote(userDetails.getUsername());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/result")
	@Operation(summary = "투표 결과 조회", description = "현재 투표의 실시간 결과를 조회합니다.")
	public ResponseEntity<?> getVoteResult() {
		try {
			MonthlyVoteResponse response = monthlyVoteService.getVoteResult();
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}
