package com.laraim237.restox.restaurant.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class RestaurantDtos {

	private RestaurantDtos() {
	}

	public record CreateRestaurantRequest(
			@NotBlank @Size(max = 150) String name,
			@Size(max = 1000) String description,
			@NotBlank @Size(max = 150) String managerFullName,
			@NotBlank @Email @Size(max = 255) String managerEmail) {
	}

	public record RestaurantResponse(
			UUID id,
			String name,
			String description,
			String photoUrl,
			boolean active,
			Instant createdAt) {
	}
}