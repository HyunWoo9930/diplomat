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
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.OdaVoteRequest;
import publicdata.hackathon.diplomats.domain.dto.response.OdaVoteResponse;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.OdaVoteService;

@RestController
@RequestMapping("/api/v1/oda-vote")
@RequiredArgsConstructor
@Tag(name = "🗳️ 투표", description = "ODA 프로젝트 투표 관련 API")
@CrossOrigin(origins = "*")
public class OdaVoteController {

	private final OdaVoteService odaVoteService;

	@PostMapping("/create")
	@Operation(summary = "월별 ODA 투표 생성", 
			   description = "이번 달 우수 ODA 프로젝트 5개(환경, 교육, 보건, 여성, 기타 분야별 1개씩)로 투표를 생성합니다. " +
			   				 "각 분야에서 매칭 점수가 가장 높은 프로젝트가 후보로 선정됩니다.")
	public ResponseEntity<String> createOdaVote() {
		try {
			String result = odaVoteService.createOdaVote();
			return ResponseEntity.ok(result);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/current")
	@Operation(summary = "현재 진행중인 ODA 투표 조회", 
			   description = "현재 진행중인 ODA 투표 정보와 5개 후보 목록을 조회합니다. " +
			   				 "각 후보의 현재 득표수와 득표율을 확인할 수 있습니다.")
	public ResponseEntity<OdaVoteResponse> getCurrentOdaVote() {
		OdaVoteResponse response = odaVoteService.getCurrentOdaVote();
		if (response == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/current/with-user-info")
	@Operation(summary = "현재 ODA 투표 조회 (사용자 투표 정보 포함)", 
			   description = "현재 진행중인 ODA 투표 정보와 함께 로그인한 사용자의 투표 여부 및 투표한 후보 정보를 조회합니다.")
	public ResponseEntity<OdaVoteResponse> getCurrentOdaVoteWithUserInfo(Authentication authentication) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		OdaVoteResponse response = odaVoteService.getCurrentOdaVoteWithUserInfo(userDetails.getUsername());
		if (response == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(response);
	}

	@PostMapping("/vote")
	@Operation(summary = "ODA 투표 참여", 
			   description = "선택한 ODA 프로젝트에 투표합니다. 사용자당 월 1회만 투표 가능하며, " +
			   				 "투표 후에는 변경할 수 없습니다. 투표 즉시 득표수에 반영됩니다.")
	public ResponseEntity<String> vote(Authentication authentication, @RequestBody OdaVoteRequest voteRequest) {
		try {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			String result = odaVoteService.vote(userDetails.getUsername(), voteRequest);
			return ResponseEntity.ok(result);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/my-vote")
	@Operation(summary = "내 ODA 투표 내역 조회", 
			   description = "현재 ODA 투표에서 내가 투표한 후보와 투표 시간을 조회합니다. " +
			   				 "아직 투표하지 않은 경우 투표 가능한 후보 목록을 표시합니다.")
	public ResponseEntity<OdaVoteResponse> getMyOdaVote(Authentication authentication) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		OdaVoteResponse response = odaVoteService.getMyOdaVote(userDetails.getUsername());
		if (response == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/result")
	@Operation(summary = "ODA 투표 결과 조회", 
			   description = "현재 ODA 투표의 실시간 결과를 조회합니다. " +
			   				 "각 후보별 득표수, 득표율, 순위 정보를 제공합니다.")
	public ResponseEntity<?> getOdaVoteResult() {
		try {
			OdaVoteResponse response = odaVoteService.getOdaVoteResult();
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/end")
	@Operation(summary = "ODA 투표 종료 (관리자용)", 
			   description = "현재 진행 중인 ODA 투표를 수동으로 종료합니다. " +
			   				 "일반적으로는 30일 후 자동 종료되지만, 필요시 관리자가 조기 종료할 수 있습니다.")
	public ResponseEntity<String> endOdaVote() {
		try {
			String result = odaVoteService.endCurrentOdaVote();
			return ResponseEntity.ok(result);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}
