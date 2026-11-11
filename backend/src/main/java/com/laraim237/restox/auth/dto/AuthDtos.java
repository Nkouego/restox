package com.laraim237.restox.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public final class AuthDtos {

	private AuthDtos() {}

	public record LoginRequest(
			@NotBlank @Email String email,
			@NotBlank String password) {}

	@Builder
	public record LoginResponse(
			String accessToken,
			String tokenType,
			long expiresIn) {}
}