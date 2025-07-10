package publicdata.hackathon.diplomats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.dto.request.CheckUserIdRequest;
import publicdata.hackathon.diplomats.domain.dto.request.JoinRequest;
import publicdata.hackathon.diplomats.domain.dto.request.LoginRequest;
import publicdata.hackathon.diplomats.domain.dto.response.CheckUserIdResponse;
import publicdata.hackathon.diplomats.jwt.JwtAuthenticationResponse;
import publicdata.hackathon.diplomats.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Tag(name = "🔐 인증/사용자", description = "회원가입, 로그인 및 사용자 인증 API")
@CrossOrigin(origins = "*")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/join")
	@Operation(summary = "회원가입", description = "새로운 사용자를 등록하고 자동으로 로그인합니다.")
	public ResponseEntity<?> join(@RequestBody JoinRequest joinRequest) {
		try {
			String response = authService.join(joinRequest);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/check-userid")
	@Operation(summary = "아이디 중복체크", description = "사용자 아이디의 중복 여부를 확인합니다.")
	public ResponseEntity<?> checkUserId(@RequestBody CheckUserIdRequest checkUserIdRequest) {
		try {
			boolean isAvailable = authService.checkUserIdAvailable(checkUserIdRequest.getUserId());
			return ResponseEntity.ok(new CheckUserIdResponse(isAvailable));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/login")
	@Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다.")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
		try {
			String response = authService.login(loginRequest);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().build();
		}
	}
}
