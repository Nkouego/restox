package com.laraim237.restox.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.laraim237.restox.auth.dto.AuthDtos.Rotation;
import com.laraim237.restox.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * @author Larissa Nkouego
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = AuthenticationException.class)
public class RefreshTokenService {

	private static final String MSG_INVALID_REFRESH_TOKEN = "Invalid refresh token";

	@Value("${app.security.refresh-token.ttl}")
	private Duration ttl;

	private final TokenRepository tokenRepository;

	public String issue(User user) {
		return createToken(user, UUID.randomUUID());
	}

	public Rotation rotate(String rawToken) {
		Token token = tokenRepository.findByTokenHash(TokenUtils.sha256Hex(rawToken))
				.orElseThrow(() -> new BadCredentialsException(MSG_INVALID_REFRESH_TOKEN));

		User user = token.getUser();

		if (token.getType() != (TokenType.REFRESH) || token.isRevoked()
				|| token.getExpiresAt().isBefore(Instant.now())) {
			log.warn("Refresh rejected: token revoked or expired, userId={}", user.getId());
			throw new BadCredentialsException(MSG_INVALID_REFRESH_TOKEN);
		}

		if (tokenRepository.markAsUsed(token.getId()) == 0) {
			log.warn("Refresh rejected: token already used, userId={}", user.getId());
			tokenRepository.revokeFamily(token.getFamilyId());
			throw new BadCredentialsException(MSG_INVALID_REFRESH_TOKEN);
		}

		UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);
		if (!userDetailsImpl.isEnabled() || !userDetailsImpl.isAccountNonLocked()) {
			log.warn("Refresh rejected: account disabled or locked, userId={}", user.getId());
			tokenRepository.revokeFamily(token.getFamilyId());
			throw new BadCredentialsException(MSG_INVALID_REFRESH_TOKEN);
		}
		return new Rotation(createToken(user, token.getFamilyId()), user);
	}

	public void revoke(String rawToken) {
		tokenRepository.findByTokenHash(TokenUtils.sha256Hex(rawToken)).ifPresent((token) -> {
			tokenRepository.revokeFamily(token.getFamilyId());
		});
	}

	private String createToken(User user, UUID familyId) {
		String rawToken = TokenUtils.generate();
		tokenRepository.save(Token.builder().user(user).type(TokenType.REFRESH).familyId(familyId)
				.tokenHash(TokenUtils.sha256Hex(rawToken)).expiresAt(Instant.now().plus(ttl)).build());
		return rawToken;
	}
}
