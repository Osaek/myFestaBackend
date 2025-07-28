package com.oseak.myFestaBackend.common.util;

import static com.oseak.myFestaBackend.common.exception.code.ClientErrorCode.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.oseak.myFestaBackend.common.exception.OsaekException;
import com.oseak.myFestaBackend.entity.CustomUserDetails;
import com.oseak.myFestaBackend.entity.Member;
import com.oseak.myFestaBackend.entity.enums.Provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secretKey;

	@Value("${jwt.expiration.access-token}")
	private Long accessTokenExpiration;

	@Value("${jwt.expiration.refresh-token}")
	private Long refreshTokenExpiration;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secretKey.getBytes());
	}

	// 토큰에서 memberId 추출
	public Long getMemberIdFromToken(String token) {
		String memberIdStr = getClaimFromToken(token, Claims::getSubject);
		return Long.parseLong(memberIdStr);
	}

	// 토큰에서 이메일 추출
	public String getEmailFromToken(String token) {
		return getClaimFromToken(token, claims -> claims.get("email", String.class));
	}

	// UserDetails 호환성을 위한 메서드
	public String getUsernameFromToken(String token) {
		return getEmailFromToken(token);
	}

	public String getNicknameFromToken(String token) {
		return getClaimFromToken(token, claims -> claims.get("nickname", String.class));
	}

	// 토큰에서 제공업체 추출
	public Provider getProviderFromToken(String token) {
		return Provider.valueOf(getClaimFromToken(token, claims -> claims.get("provider", String.class)));
	}

	// 토큰에서 만료일 추출
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	// 토큰에서 특정 클레임 추출
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	// 토큰에서 모든 클레임 추출
	private Claims getAllClaimsFromToken(String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException e) {
			throw new OsaekException(JWT_TOKEN_RETIRED, e);
		} catch (UnsupportedJwtException | MalformedJwtException e) {
			throw new OsaekException(JWT_TOKEN_INVALID, e);
		} catch (SecurityException e) {
			throw new OsaekException(JWT_TOKEN_SIGN_INVALID, e);
		} catch (IllegalArgumentException e) {
			throw new OsaekException(JWT_TOKEN_NOT_FOUND, e);
		}
	}

	// 토큰 만료 여부 확인
	public Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	// Member 정보로 액세스 토큰 생성
	public String generateAccessToken(Member member) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("email", member.getEmail());
		claims.put("nickname", member.getNickname());
		claims.put("provider", member.getProvider().name());
		claims.put("tokenType", "ACCESS");
		return createToken(claims, String.valueOf(member.getId()), accessTokenExpiration);
	}

	// Member 정보로 리프레시 토큰 생성
	public String generateRefreshToken(Member member) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("memberId", member.getEmail());
		claims.put("nickname", member.getNickname());
		claims.put("provider", member.getProvider().name());
		claims.put("tokenType", "REFRESH");
		return createToken(claims, String.valueOf(member.getId()), refreshTokenExpiration);
	}

	// UserDetails로 토큰 생성 (호환성을 위해 유지)
	public String generateAccessToken(UserDetails userDetails) {
		if (userDetails instanceof CustomUserDetails customUserDetails) {
			return generateAccessToken(customUserDetails.getMember());
		}

		Map<String, Object> claims = new HashMap<>();
		claims.put("email", userDetails.getUsername());
		claims.put("tokenType", "ACCESS");
		return createToken(claims, "0", accessTokenExpiration);
	}

	// UserDetails로 토큰 생성 (호환성을 위해 유지)
	public String generateRefreshToken(UserDetails userDetails) {
		if (userDetails instanceof CustomUserDetails customUserDetails) {
			return generateRefreshToken(customUserDetails.getMember());
		}

		Map<String, Object> claims = new HashMap<>();
		claims.put("email", userDetails.getUsername());
		claims.put("tokenType", "REFRESH");
		return createToken(claims, "0", refreshTokenExpiration);
	}

	// 기존 메서드들은 액세스 토큰을 생성하도록 수정
	public String generateToken(Member member) {
		return generateAccessToken(member);
	}

	public String generateToken(UserDetails userDetails) {
		return generateAccessToken(userDetails);
	}

	public String generateToken(String email) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("email", email);
		claims.put("tokenType", "ACCESS");
		return createToken(claims, "0", accessTokenExpiration);
	}

	public String generateToken(Long memberId) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("tokenType", "ACCESS");
		return createToken(claims, String.valueOf(memberId), accessTokenExpiration);
	}

	public String generateToken(String email, Map<String, Object> extraClaims) {
		extraClaims.put("email", email);
		extraClaims.put("tokenType", "ACCESS");
		return createToken(extraClaims, "0", accessTokenExpiration);
	}

	public String generateToken(Long memberId, Map<String, Object> extraClaims) {
		extraClaims.put("tokenType", "ACCESS");
		return createToken(extraClaims, String.valueOf(memberId), accessTokenExpiration);
	}

	// 실제 토큰 생성 로직
	private String createToken(Map<String, Object> claims, String subject, Long expiration) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expiration);

		return Jwts.builder()
			.setClaims(claims)
			.setSubject(subject)
			.setIssuedAt(now)
			.setExpiration(expiryDate)
			.signWith(getSigningKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	// 토큰 타입 확인
	public String getTokenType(String token) {
		return getClaimFromToken(token, claims -> claims.get("tokenType", String.class));
	}

	// 엑세스 토큰인지 확인
	public boolean isAccessToken(String token) {
		String tokenType = getTokenType(token);
		return "ACCESS".equals(tokenType);
	}

	public boolean isRefreshToken(String token) {
		String tokenType = getTokenType(token);
		return "REFRESH".equals(tokenType);
	}

	// 토큰 유효성 검증 (Member용)
	public Boolean validateToken(String token, Member member) {
		final Long memberId = getMemberIdFromToken(token);
		return (memberId.equals(member.getId()) && !isTokenExpired(token) && !member.isWithdrawn());
	}

	// 토큰 유효성 검증 (UserDetails용)
	public Boolean validateToken(String token, UserDetails userDetails) {
		if (userDetails instanceof CustomUserDetails customUserDetails) {
			return validateToken(token, customUserDetails.getMember());
		}

		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	// 토큰 유효성 검증 (이메일로)
	public Boolean validateToken(String token, String email) {
		final String tokenEmail = getEmailFromToken(token);
		return (tokenEmail.equals(email) && !isTokenExpired(token));
	}

	// 토큰 유효성 검증 (기본)
	public Boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	// 토큰에서 Bearer 제거
	public String resolveToken(String bearerToken) {
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	public String refreshAccessToken(String refreshToken) {
		if (!validateToken(refreshToken) || !isRefreshToken(refreshToken)) {
			throw new OsaekException(JWT_REFRESH_TOKEN_INVALID);
		}

		Long memberId = getMemberIdFromToken(refreshToken);
		String email = getEmailFromToken(refreshToken);
		String nickname = getNicknameFromToken(refreshToken);
		Provider provider = getProviderFromToken(refreshToken);

		Map<String, Object> claims = new HashMap<>();
		claims.put("email", email);
		claims.put("nickname", nickname);
		claims.put("provider", provider);
		claims.put("tokenType", "ACCESS");

		return createToken(claims, String.valueOf(memberId), accessTokenExpiration);
	}
}
