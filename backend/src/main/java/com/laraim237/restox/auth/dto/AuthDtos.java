package com.laraim237.restox.auth.dto;

import java.util.Set;
import java.util.UUID;

import com.laraim237.restox.restaurant.dto.RestaurantSummary;

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
			long expiresIn) {
		
	}
	
	public record CurrentUserResponse(
			UUID id,
			String email,
			String fullName,
			String photoUrl,
			boolean platformAdmin,
			RestaurantSummary restaurant,
			Set<String> permissions) {
	}
}