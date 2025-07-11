package publicdata.hackathon.diplomats.jwt;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.exception.CustomException;
import publicdata.hackathon.diplomats.exception.ErrorCode;
import publicdata.hackathon.diplomats.exception.ErrorResponse;
import publicdata.hackathon.diplomats.service.CustomUserDetailsService;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider tokenProvider;
	private final CustomUserDetailsService customUserDetailsService;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws IOException, ServletException {
		
		try {
			String jwt = getJwtFromRequest(request);

			if (jwt != null && !jwt.trim().isEmpty()) {
				try {
					// 토큰 유효성 검사
					if (tokenProvider.validateToken(jwt)) {
						String userId = tokenProvider.getUserIdFromJWT(jwt);

						User user = customUserDetailsService.findByUserId(userId);
						if (user == null) {
							log.warn("User not found for userId: {}", userId);
							handleAuthenticationError(response, ErrorCode.USER_NOT_FOUND, request.getRequestURI());
							return;
						}

						UserDetails userDetails = new CustomUserDetails(user);
						JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails, null,
							userDetails.getAuthorities());
						authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authentication);
						
						log.debug("Successfully authenticated user: {}", userId);
					}
				} catch (CustomException e) {
					log.error("JWT Authentication failed: {}", e.getMessage());
					handleAuthenticationError(response, e.getErrorCode(), request.getRequestURI());
					return;
				}
			}
			// 토큰이 없으면 익명 사용자로 처리 (필터 체인 계속 진행)

			filterChain.doFilter(request, response);
			
		} catch (Exception e) {
			log.error("Unexpected error during JWT authentication: {}", e.getMessage(), e);
			handleAuthenticationError(response, ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI());
		}
	}

	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	private void handleAuthenticationError(HttpServletResponse response, ErrorCode errorCode, String path) 
			throws IOException {
		response.setStatus(errorCode.getStatus().value());
		response.setContentType("application/json;charset=UTF-8");
		
		ErrorResponse errorResponse = ErrorResponse.of(errorCode, path);
		String jsonResponse = objectMapper.writeValueAsString(errorResponse);
		
		response.getWriter().write(jsonResponse);
	}
}
