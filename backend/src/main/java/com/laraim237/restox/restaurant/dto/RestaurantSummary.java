package com.laraim237.restox.restaurant.dto;

import java.util.UUID;

public record RestaurantSummary(
		UUID id, 
		String name) {}