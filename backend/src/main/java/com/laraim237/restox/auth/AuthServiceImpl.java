package com.laraim237.restox.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.laraim237.restox.audit.AuditAction;
import com.laraim237.restox.audit.AuditService;
import com.laraim237.restox.auth.dto.AuthDtos.AuthResponse;
import com.laraim237.restox.auth.dto.AuthDtos.LoginRequest;
import com.laraim237.restox.auth.dto.AuthDtos.Rotation;

import lombok.RequiredArgsConstructor;

/*
 * @author Larissa Nkouego
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private static final String MSG_INVALID_REFRESH_TOKEN = "Invalid refresh token";
	
	private final AuthenticationManager authenticationManager;
	private final LoginAttemptService loginAttemptService;
	private final RefreshTokenService refreshTokenService;
	private final AuditService auditService;
	private final JwtService jwtService;

	@Override
	public AuthResponse login(LoginRequest request) {
		Authentication authentication;

		try {
			authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
		} catch (BadCredentialsException ex) {
			loginAttemptService.recordFailure(request.email());
			throw ex;
		}

		UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
		loginAttemptService.recordSuccess(principal.getUser().getId());

		
		auditService.record(AuditAction.LOGIN_SUCCESS, principal.getUser().getId());
		
		String refreshToken = refreshTokenService.issue(principal.getUser());

		return buildAuthResponse(principal, refreshToken);
	}

	@Override
	public AuthResponse refresh(String refreshToken) {
		
		if(refreshToken == null || refreshToken.isBlank()) {
			throw new BadCredentialsException(MSG_INVALID_REFRESH_TOKEN);
		}
		
		Rotation rotation = refreshTokenService.rotate(refreshToken);
		
		UserDetailsImpl principal = new UserDetailsImpl(rotation.user());
		
		return buildAuthResponse(principal, rotation.refreshToken());
	}

	@Override
	public void logout(String refreshToken) {
		if (refreshToken != null && !refreshToken.isBlank()) {
			refreshTokenService.revoke(refreshToken);
		}
	}
	
	private AuthResponse buildAuthResponse(UserDetailsImpl principal, String refreshToken) {
		return AuthResponse.builder()
				.accessToken(jwtService.generateAccessToken(principal))
				.tokenType("Bearer")
				.expiresIn(jwtService.getAccessTokenTtl().toSeconds())
				.refreshToken(refreshToken)
				.build();
	}
}
