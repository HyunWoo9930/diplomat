package publicdata.hackathon.diplomats.jwt;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.exception.CustomException;
import publicdata.hackathon.diplomats.exception.ErrorCode;

@Slf4j
@Component
public class JwtTokenProvider {

	@Value("${spring.jwt.secret}")
	private String jwtSecret;

	@Value("${spring.jwt.access.expiration}")
	private long jwtExpirationInMs;
	
	@Value("${spring.jwt.refresh.expiration}")
	private long refreshExpirationInMs;

	public String generateToken(User user) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

		return Jwts.builder()
			.setSubject(user.getUserId())
			.setIssuedAt(new Date())
			.setExpiration(expiryDate)
			.claim("type", "access")
			.signWith(SignatureAlgorithm.HS512, jwtSecret)
			.compact();
	}
	
	public String generateRefreshToken(User user) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + refreshExpirationInMs);

		return Jwts.builder()
			.setSubject(user.getUserId())
			.setIssuedAt(new Date())
			.setExpiration(expiryDate)
			.claim("type", "refresh")
			.signWith(SignatureAlgorithm.HS512, jwtSecret)
			.compact();
	}
	
	public long getAccessTokenExpirationInSeconds() {
		return jwtExpirationInMs / 1000;
	}

	public String getUserIdFromJWT(String token) {
		try {
			Claims claims = Jwts.parser()
				.setSigningKey(jwtSecret)
				.parseClaimsJws(token)
				.getBody();

			return claims.getSubject();
		} catch (ExpiredJwtException e) {
			log.error("JWT token is expired: {}", e.getMessage());
			throw new CustomException(ErrorCode.EXPIRED_TOKEN);
		} catch (UnsupportedJwtException e) {
			log.error("JWT token is unsupported: {}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		} catch (MalformedJwtException e) {
			log.error("JWT token is malformed: {}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		} catch (SignatureException e) {
			log.error("JWT signature does not match: {}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		} catch (IllegalArgumentException e) {
			log.error("JWT token compact of handler are invalid: {}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}

	public boolean validateToken(String authToken) {
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
			return true;
		} catch (ExpiredJwtException e) {
			log.error("JWT token is expired: {}", e.getMessage());
			throw new CustomException(ErrorCode.EXPIRED_TOKEN);
		} catch (UnsupportedJwtException e) {
			log.error("JWT token is unsupported: {}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		} catch (MalformedJwtException e) {
			log.error("JWT token is malformed: {}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		} catch (SignatureException e) {
			log.error("JWT signature does not match: {}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		} catch (IllegalArgumentException e) {
			log.error("JWT token compact of handler are invalid: {}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}

	public boolean validateRefreshToken(String refreshToken) {
		try {
			Claims claims = Jwts.parser()
				.setSigningKey(jwtSecret)
				.parseClaimsJws(refreshToken)
				.getBody();
			
			// 리프레시 토큰인지 확인
			String tokenType = claims.get("type", String.class);
			if (!"refresh".equals(tokenType)) {
				throw new CustomException(ErrorCode.INVALID_TOKEN);
			}
			
			return true;
		} catch (ExpiredJwtException e) {
			log.error("Refresh token is expired: {}", e.getMessage());
			throw new CustomException(ErrorCode.EXPIRED_TOKEN);
		} catch (Exception e) {
			log.error("Invalid refresh token: {}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}
	
	public boolean isTokenExpired(String token) {
		try {
			Claims claims = Jwts.parser()
				.setSigningKey(jwtSecret)
				.parseClaimsJws(token)
				.getBody();
			
			return claims.getExpiration().before(new Date());
		} catch (ExpiredJwtException e) {
			return true;
		} catch (Exception e) {
			return true;
		}
	}
}
