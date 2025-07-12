package publicdata.hackathon.diplomats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.request.CheckUserIdRequest;
import publicdata.hackathon.diplomats.domain.dto.request.JoinRequest;
import publicdata.hackathon.diplomats.domain.dto.request.LoginRequest;
import publicdata.hackathon.diplomats.domain.dto.request.RefreshTokenRequest;
import publicdata.hackathon.diplomats.domain.dto.response.CheckUserIdResponse;
import publicdata.hackathon.diplomats.exception.CustomException;
import publicdata.hackathon.diplomats.exception.ErrorCode;
import publicdata.hackathon.diplomats.jwt.JwtAuthenticationResponse;
import publicdata.hackathon.diplomats.service.AuthService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Tag(name = "🔐 인증/사용자", description = "회원가입, 로그인 및 사용자 인증 API")
@CrossOrigin(origins = "*")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/join")
	@Operation(summary = "회원가입", description = "새로운 사용자를 등록하고 자동으로 로그인합니다.")
	public ResponseEntity<JwtAuthenticationResponse> join(@Valid @RequestBody JoinRequest joinRequest) {
		log.info("회원가입 요청: userId={}", joinRequest != null ? joinRequest.getUserId() : "null");
		
		JwtAuthenticationResponse response = authService.join(joinRequest);
		log.info("회원가입 성공: userId={}", joinRequest.getUserId());
		
		return ResponseEntity.ok(response);
	}

	@PostMapping("/check-userid")
	@Operation(summary = "아이디 중복체크", description = "사용자 아이디의 중복 여부를 확인합니다.")
	public ResponseEntity<CheckUserIdResponse> checkUserId(@Valid @RequestBody CheckUserIdRequest checkUserIdRequest) {
		log.info("아이디 중복체크 요청: userId={}", 
			checkUserIdRequest != null ? checkUserIdRequest.getUserId() : "null");
		
		boolean isAvailable = authService.checkUserIdAvailable(checkUserIdRequest.getUserId());
		
		log.info("아이디 중복체크 결과: userId={}, available={}", 
			checkUserIdRequest.getUserId(), isAvailable);
			
		return ResponseEntity.ok(new CheckUserIdResponse(isAvailable));
	}

	@PostMapping("/login")
	@Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다.")
	public ResponseEntity<JwtAuthenticationResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
		log.info("로그인 요청: userId={}", loginRequest != null ? loginRequest.getUserId() : "null");
		
		JwtAuthenticationResponse response = authService.login(loginRequest);
		log.info("로그인 성공: userId={}", loginRequest.getUserId());
		
		return ResponseEntity.ok(response);
	}

	@PostMapping("/refresh")
	@Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다.")
	public ResponseEntity<JwtAuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
		log.info("토큰 갱신 요청");
		
		try {
			if (request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
				log.error("리프레시 토큰이 비어있음");
				throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD, "리프레시 토큰이 필요합니다.");
			}
			
			JwtAuthenticationResponse response = authService.refreshToken(request.getRefreshToken());
			log.info("토큰 갱신 성공");
			
			return ResponseEntity.ok(response);
			
		} catch (CustomException e) {
			log.error("토큰 갱신 실패 - CustomException: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("토큰 갱신 실패 - 예상치 못한 오류: {}", e.getMessage(), e);
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 갱신 중 오류가 발생했습니다.");
		}
	}
}
