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

	public String generateToken(User user) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

		return Jwts.builder()
			.setSubject(user.getUserId())
			.setIssuedAt(new Date())
			.setExpiration(expiryDate)
			.signWith(SignatureAlgorithm.HS512, jwtSecret)
			.compact();
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
