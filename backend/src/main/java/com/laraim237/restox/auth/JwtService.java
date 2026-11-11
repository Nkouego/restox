package com.laraim237.restox.auth;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.laraim237.restox.user.User;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtEncoder jwtEncoder;
	
	@Value("${app.security.jwt.access-token-ttl}")
	private Duration accessTokenTtl;
    
	public String generateAccessToken(UserDetailsImpl principal) {
		User user = principal.getUser();
		Instant now = Instant.now();

		JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
				.issuer("restox")
				.subject(user.getId().toString())
				.issuedAt(now)
				.expiresAt(now.plus(accessTokenTtl));
		
		if(!user.isPlatformAdmin()) {
			claims.claim("restaurantId", user.getRestaurant().getId().toString());
		}
		
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
	}
	
	public Duration getAccessTokenTtl() {
		return accessTokenTtl;
	}
}
