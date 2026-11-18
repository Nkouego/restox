package com.laraim237.restox.auth;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.laraim237.restox.auth.dto.AuthDtos.AuthResponse;
import com.laraim237.restox.auth.dto.AuthDtos.CurrentUserResponse;
import com.laraim237.restox.auth.dto.AuthDtos.LoginRequest;
import com.laraim237.restox.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final RefreshCookieFactory cookieFactory;

	private final AuthService authService;
	private final UserService userService;


	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return toResponse(authService.login(request));
	}

	@GetMapping("/me")
	public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(userService.getCurrentUser(UUID.fromString(jwt.getSubject())));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(
			@CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
		return toResponse(authService.refresh(refreshToken));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			@CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
		authService.logout(refreshToken);
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookieFactory.clearCookie().toString())
				.build();
	}
	
	private ResponseEntity<AuthResponse> toResponse(AuthResponse authResponse) {
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookieFactory.createCookie(authResponse.refreshToken()).toString())
				.body(authResponse);
	}

}
