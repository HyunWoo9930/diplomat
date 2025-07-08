package publicdata.hackathon.diplomats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.response.CommunityResponse;
import publicdata.hackathon.diplomats.service.CommunityService;

@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
@Tag(name = "커뮤니티", description = "커뮤니티 메인페이지 관련 API")
public class CommunityController {

	private final CommunityService communityService;

	@GetMapping("/")
	@Operation(summary = "커뮤니티 메인페이지", description = "자유게시판과 토론게시판의 인기글 상위 3개씩을 조회합니다.")
	public ResponseEntity<CommunityResponse> getCommunityMain() {
		CommunityResponse response = communityService.getCommunityData();
		return ResponseEntity.ok(response);
	}
}
