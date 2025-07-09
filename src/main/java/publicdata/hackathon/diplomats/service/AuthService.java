package publicdata.hackathon.diplomats.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.request.JoinRequest;
import publicdata.hackathon.diplomats.domain.dto.request.LoginRequest;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.jwt.JwtTokenProvider;
import publicdata.hackathon.diplomats.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public String register(JoinRequest joinRequest) {
		return join(joinRequest);
	}

	public String join(JoinRequest joinRequest) {
		log.info("회원가입 시작: userId={}", joinRequest.getUserId());

		// 중복 체크
		if (userRepository.existsByUserId(joinRequest.getUserId())) {
			log.warn("이미 존재하는 사용자 ID: {}", joinRequest.getUserId());
			throw new RuntimeException("이미 존재하는 사용자 ID입니다.");
		}

		// 사용자 생성
		User user = User.builder()
			.userId(joinRequest.getUserId())
			.password(passwordEncoder.encode(joinRequest.getPassword()))
			.name(joinRequest.getName())
			.build();

		User savedUser = userRepository.save(user);
		log.info("사용자 저장 완료: userId={}, id={}", savedUser.getUserId(), savedUser.getId());

		// 회원가입 후 자동 로그인
		return authenticateUser(joinRequest.getUserId(), joinRequest.getPassword());
	}

	public String login(LoginRequest loginRequest) {
		log.info("로그인 시작: userId={}", loginRequest.getUserId());
		return authenticateUser(loginRequest.getUserId(), loginRequest.getPassword());
	}

	private String authenticateUser(String userId, String password) {
		log.info("인증 시작: userId={}", userId);

		try {
			// 사용자 존재 확인
			User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> {
					log.error("사용자를 찾을 수 없음: userId={}", userId);
					return new RuntimeException("사용자를 찾을 수 없습니다.");
				});

			log.info("사용자 찾기 성공: userId={}, name={}", user.getUserId(), user.getName());

			// 비밀번호 직접 검증
			if (!passwordEncoder.matches(password, user.getPassword())) {
				log.error("비밀번호 불일치: userId={}", userId);
				throw new RuntimeException("비밀번호가 일치하지 않습니다.");
			}

			log.info("비밀번호 검증 성공");

			// JWT 토큰 생성
			String response = jwtTokenProvider.generateToken(user);
			log.info("JWT 토큰 생성 완료");

			return response;

		} catch (Exception e) {
			log.error("인증 실패: userId={}, error={}", userId, e.getMessage(), e);
			throw new RuntimeException("인증에 실패했습니다: " + e.getMessage());
		}
	}
}
