package publicdata.hackathon.diplomats.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		log.info("loadUserByUsername 호출: userId={}", userId);
		
		User user = userRepository.findByUserId(userId)
			.orElseThrow(() -> {
				log.error("사용자를 찾을 수 없음: userId={}", userId);
				return new UsernameNotFoundException("User not found with userId: " + userId);
			});
		
		log.info("사용자 찾기 성공: userId={}, name={}", user.getUserId(), user.getUserId());
		return new CustomUserDetails(user);
	}

	public User findByUserName(String userName) {
		return userRepository.findByUserId(userName).orElse(null);
	}
	
	public User findByUserId(String userId) {
		return userRepository.findByUserId(userId).orElse(null);
	}
}
