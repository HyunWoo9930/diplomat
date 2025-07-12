package publicdata.hackathon.diplomats.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.dto.request.JoinRequest;
import publicdata.hackathon.diplomats.domain.dto.request.LoginRequest;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.exception.CustomException;
import publicdata.hackathon.diplomats.exception.ErrorCode;
import publicdata.hackathon.diplomats.jwt.JwtAuthenticationResponse;
import publicdata.hackathon.diplomats.jwt.JwtTokenProvider;
import publicdata.hackathon.diplomats.repository.LikeRepository;
import publicdata.hackathon.diplomats.repository.NewsScrapRepository;
import publicdata.hackathon.diplomats.repository.UserLevelHistoryRepository;
import publicdata.hackathon.diplomats.repository.UserOdaVoteRepository;
import publicdata.hackathon.diplomats.repository.UserRepository;
import publicdata.hackathon.diplomats.repository.UserStampRepository;
import publicdata.hackathon.diplomats.repository.UserVoteRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	
	private final LikeRepository likeRepository;
	private final NewsScrapRepository newsScrapRepository;
	private final UserStampRepository userStampRepository;
	private final UserLevelHistoryRepository userLevelHistoryRepository;
	private final UserVoteRepository userVoteRepository;
	private final UserOdaVoteRepository userOdaVoteRepository;

	public JwtAuthenticationResponse register(JoinRequest joinRequest) {
		return join(joinRequest);
	}

	public JwtAuthenticationResponse join(JoinRequest joinRequest) {
		log.info("회원가입 시작: userId={}", joinRequest.getUserId());

		// 입력값 유효성 검사
		validateJoinRequest(joinRequest);

		// 중복 체크
		if (userRepository.existsByUserId(joinRequest.getUserId())) {
			log.warn("이미 존재하는 사용자 ID: {}", joinRequest.getUserId());
			throw new CustomException(ErrorCode.DUPLICATE_USER_ID);
		}

		try {
			// 사용자 생성
			User user = User.builder()
				.userId(joinRequest.getUserId())
				.password(passwordEncoder.encode(joinRequest.getPassword()))
				.build();

			User savedUser = userRepository.save(user);
			log.info("사용자 저장 완료: userId={}, id={}", savedUser.getUserId(), savedUser.getId());

			// 회원가입 후 자동 로그인
			return authenticateUser(joinRequest.getUserId(), joinRequest.getPassword());
		} catch (Exception e) {
			log.error("회원가입 실패: userId={}, error={}", joinRequest.getUserId(), e.getMessage(), e);
			throw new CustomException(ErrorCode.DATABASE_ERROR, "회원가입 중 오류가 발생했습니다.");
		}
	}

	public JwtAuthenticationResponse login(LoginRequest loginRequest) {
		log.info("로그인 시작: userId={}", loginRequest.getUserId());
		
		// 입력값 유효성 검사
		validateLoginRequest(loginRequest);
		
		return authenticateUser(loginRequest.getUserId(), loginRequest.getPassword());
	}

	public boolean checkUserIdAvailable(String userId) {
		log.info("아이디 중복체크: userId={}", userId);
		
		if (userId == null || userId.trim().isEmpty()) {
			throw new CustomException(ErrorCode.INVALID_INPUT, "아이디는 필수 입력값입니다.");
		}
		
		return !userRepository.existsByUserId(userId);
	}

	private JwtAuthenticationResponse authenticateUser(String userId, String password) {
		log.info("인증 시작: userId={}", userId);

		try {
			// 사용자 존재 확인
			User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> {
					log.error("사용자를 찾을 수 없음: userId={}", userId);
					return new CustomException(ErrorCode.INVALID_CREDENTIALS);
				});

			log.info("사용자 찾기 성공: userId={}", user.getUserId());

			// 비밀번호 검증
			if (!passwordEncoder.matches(password, user.getPassword())) {
				log.error("비밀번호 불일치: userId={}", userId);
				throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
			}

			log.info("비밀번호 검증 성공");

			// JWT 토큰 생성 (액세스 토큰 + 리프레시 토큰)
			String accessToken = jwtTokenProvider.generateToken(user);
			String refreshToken = jwtTokenProvider.generateRefreshToken(user);
			log.info("JWT 토큰 생성 완료");

			return JwtAuthenticationResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.tokenType("Bearer")
				.expiresIn(jwtTokenProvider.getAccessTokenExpirationInSeconds())
				.build();

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("인증 실패: userId={}, error={}", userId, e.getMessage(), e);
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "인증 처리 중 오류가 발생했습니다.");
		}
	}
	
	/**
	 * 리프레시 토큰으로 새로운 액세스 토큰 발급
	 */
	public JwtAuthenticationResponse refreshToken(String refreshToken) {
		log.info("토큰 갱신 시작");
		
		try {
			// 리프레시 토큰 유효성 검증
			if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
				throw new CustomException(ErrorCode.INVALID_TOKEN);
			}
			
			// 리프레시 토큰에서 사용자 ID 추출
			String userId = jwtTokenProvider.getUserIdFromJWT(refreshToken);
			
			// 사용자 존재 확인
			User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
			
			// 새로운 액세스 토큰 생성
			String newAccessToken = jwtTokenProvider.generateToken(user);
			log.info("새로운 액세스 토큰 생성 완료: userId={}", userId);
			
			return JwtAuthenticationResponse.builder()
				.accessToken(newAccessToken)
				.refreshToken(refreshToken) // 기존 리프레시 토큰 재사용
				.tokenType("Bearer")
				.expiresIn(jwtTokenProvider.getAccessTokenExpirationInSeconds())
				.build();
				
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("토큰 갱신 실패: error={}", e.getMessage(), e);
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 갱신 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 회원 탈퇴
	 */
	public void withdrawUser(String userId, String password) {
		log.info("회원 탈퇴 시작: userId={}", userId);
		
		try {
			// 사용자 존재 확인
			User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> {
					log.error("탈퇴 요청한 사용자를 찾을 수 없음: userId={}", userId);
					return new CustomException(ErrorCode.USER_NOT_FOUND);
				});
			
			// 비밀번호 검증 (본인 확인)
			if (!passwordEncoder.matches(password, user.getPassword())) {
				log.error("회원 탈퇴 시 비밀번호 불일치: userId={}", userId);
				throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "비밀번호가 일치하지 않습니다.");
			}
			
			// 연관된 데이터들을 순서대로 삭제
			deleteUserRelatedData(user);
			
			// 사용자 삭제
			userRepository.delete(user);
			
			log.info("회원 탈퇴 완료: userId={}", userId);
			
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("회원 탈퇴 실패: userId={}, error={}", userId, e.getMessage(), e);
			throw new CustomException(ErrorCode.DATABASE_ERROR, "회원 탈퇴 처리 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 사용자와 연관된 모든 데이터 삭제
	 */
	private void deleteUserRelatedData(User user) {
		Long userId = user.getId();
		String userIdString = user.getUserId();
		
		try {
			// 1. 좋아요 데이터 삭제
			likeRepository.deleteByUser(user);
			log.info("사용자 좋아요 데이터 삭제 완료: userId={}", userIdString);
			
			// 2. 뉴스 스크랩 데이터 삭제
			newsScrapRepository.deleteByUser(user);
			log.info("사용자 뉴스 스크랩 데이터 삭제 완료: userId={}", userIdString);
			
			// 3. 사용자 스탬프 데이터 삭제
			userStampRepository.deleteByUser(user);
			log.info("사용자 스탬프 데이터 삭제 완료: userId={}", userIdString);
			
			// 4. 사용자 레벨 히스토리 삭제
			userLevelHistoryRepository.deleteByUser(user);
			log.info("사용자 레벨 히스토리 삭제 완료: userId={}", userIdString);
			
			// 5. 사용자 투표 데이터 삭제
			userVoteRepository.deleteByUser(user);
			log.info("사용자 투표 데이터 삭제 완료: userId={}", userIdString);
			
			// 6. 사용자 ODA 투표 데이터 삭제
			userOdaVoteRepository.deleteByUser(user);
			log.info("사용자 ODA 투표 데이터 삭제 완료: userId={}", userIdString);
			
		} catch (Exception e) {
			log.error("사용자 연관 데이터 삭제 실패: userId={}, error={}", userIdString, e.getMessage(), e);
			throw new CustomException(ErrorCode.DATABASE_ERROR, "연관 데이터 삭제 중 오류가 발생했습니다.");
		}
	}

	private void validateJoinRequest(JoinRequest joinRequest) {
		if (joinRequest == null) {
			throw new CustomException(ErrorCode.INVALID_INPUT, "회원가입 정보가 필요합니다.");
		}
		
		if (joinRequest.getUserId() == null || joinRequest.getUserId().trim().isEmpty()) {
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD, "아이디는 필수 입력값입니다.");
		}
		
		if (joinRequest.getPassword() == null || joinRequest.getPassword().trim().isEmpty()) {
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD, "비밀번호는 필수 입력값입니다.");
		}
		
		// 아이디 길이 검증
		if (joinRequest.getUserId().length() < 4 || joinRequest.getUserId().length() > 20) {
			throw new CustomException(ErrorCode.INVALID_INPUT, "아이디는 4자 이상 20자 이하로 입력해주세요.");
		}
		
		// 비밀번호 길이 검증
		if (joinRequest.getPassword().length() < 6) {
			throw new CustomException(ErrorCode.INVALID_INPUT, "비밀번호는 6자 이상으로 입력해주세요.");
		}
	}

	private void validateLoginRequest(LoginRequest loginRequest) {
		if (loginRequest == null) {
			throw new CustomException(ErrorCode.INVALID_INPUT, "로그인 정보가 필요합니다.");
		}
		
		if (loginRequest.getUserId() == null || loginRequest.getUserId().trim().isEmpty()) {
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD, "아이디는 필수 입력값입니다.");
		}
		
		if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
			throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD, "비밀번호는 필수 입력값입니다.");
		}
	}
}
