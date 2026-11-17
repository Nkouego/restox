package com.laraim237.restox.auth;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.laraim237.restox.auth.dto.AuthDtos.CurrentUserResponse;
import com.laraim237.restox.auth.dto.AuthDtos.LoginRequest;
import com.laraim237.restox.auth.dto.AuthDtos.LoginResponse;
import com.laraim237.restox.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	
	private final AuthService authService;
	private final UserService userService;

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
		return ResponseEntity.ok(authService.login(request));	
	}
	
	@GetMapping("/me")
	public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(userService.getCurrentUser(UUID.fromString(jwt.getSubject())));
	}
	
}
