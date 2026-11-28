package com.laraim237.restox.restaurant;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.laraim237.restox.restaurant.dto.RestaurantDtos.CreateRestaurantRequest;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.RestaurantFilterRequest;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.RestaurantResponse;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.UpdateRestaurantRequest;

public interface RestaurantService {
	
	RestaurantResponse createWithManager(CreateRestaurantRequest request);

	Page<RestaurantResponse> list(RestaurantFilterRequest request, Pageable pageable);

	RestaurantResponse getById(UUID restaurantId);
	
	RestaurantResponse update(UUID restaurantId, UpdateRestaurantRequest request, UUID actorId);

	void activate(UUID restaurantId, UUID actorId);

	void deactivate(UUID restaurantId, UUID actorId);
}
