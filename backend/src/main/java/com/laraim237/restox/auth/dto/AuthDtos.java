package com.laraim237.restox.auth.dto;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.laraim237.restox.restaurant.dto.RestaurantSummary;
import com.laraim237.restox.user.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/*
 * @author Larissa Nkouego
 */

public final class AuthDtos {

	private AuthDtos() {}

	public record LoginRequest(
			@NotBlank @Email String email,
			@NotBlank String password) {}

	@Builder
	public record AuthResponse(
			String accessToken,
			String tokenType,
			long expiresIn,
			@JsonIgnore String refreshToken) {
		
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
	
	public record Rotation(
		    String refreshToken,
		    User user
		) {}
}