package publicdata.hackathon.diplomats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.request.LoginRequest;
import publicdata.hackathon.diplomats.domain.dto.response.ApiResponse;
import publicdata.hackathon.diplomats.domain.dto.response.UserInfoResponse;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.AuthService;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "👤 마이페이지", description = "사용자 정보 및 개인 정보 관리")
@CrossOrigin(origins = "*")
public class UserController {

	private final AuthService authService;

	@DeleteMapping("/withdraw")
	@Operation(
		summary = "회원 탈퇴", 
		description = "회원 탈퇴를 진행합니다. 본인 확인을 위해 비밀번호가 필요합니다.",
		security = @SecurityRequirement(name = "Bearer Authentication")
	)
	public ResponseEntity<ApiResponse<String>> withdrawUser(
		Authentication authentication,
		@Valid @RequestBody LoginRequest withdrawRequest
	) {
		try {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			log.info("회원 탈퇴 요청: userId={}", userDetails.getUsername());
			
			authService.withdrawUser(userDetails.getUsername(), withdrawRequest.getPassword());
			
			return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다."));
			
		} catch (Exception e) {
			log.error("회원 탈퇴 실패: error={}", e.getMessage(), e);
			return ResponseEntity.badRequest().body(
				ApiResponse.error("회원 탈퇴에 실패했습니다: " + e.getMessage(), null)
			);
		}
	}
}
