package com.laraim237.restox.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.laraim237.restox.audit.AuditAction;
import com.laraim237.restox.audit.AuditService;
import com.laraim237.restox.auth.dto.AuthDtos.LoginRequest;
import com.laraim237.restox.auth.dto.AuthDtos.LoginResponse;

import lombok.RequiredArgsConstructor;

/*
 * @author Larissa Nkouego
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final AuthenticationManager authenticationManager;
	private final LoginAttemptService loginAttemptService;
	private final AuditService auditService;
	private final JwtService jwtService;

	@Override
	public LoginResponse login(LoginRequest request) {
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

		String accessToken = jwtService.generateAccessToken(principal);
		
		auditService.record(AuditAction.LOGIN_SUCCESS, principal.getUser().getId());

		return LoginResponse.builder()
				.accessToken(accessToken)
				.tokenType("Bearer")
				.expiresIn(jwtService.getAccessTokenTtl().toSeconds())
				.build();
	}
}
