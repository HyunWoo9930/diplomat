package publicdata.hackathon.diplomats.jwt;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import publicdata.hackathon.diplomats.domain.entity.User;

@Getter
public class CustomUserDetails implements UserDetails {

	private final User user;
	private final String id;
	private final String password;
	private final String citizenType;
	private final Collection<? extends GrantedAuthority> authorities;

	public CustomUserDetails(User user) {
		this.user = user;  // User 객체 저장
		this.password = user.getPassword();
		this.id = user.getUserId();
		this.citizenType = user.getCitizenType();
		this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")); // 권한 설정 필요 시 추가
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return id;
	}

	public String getCitizenType() {
		return citizenType;
	}

	/**
	 * User 엔티티 반환
	 */
	public User getUser() {
		return user;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
