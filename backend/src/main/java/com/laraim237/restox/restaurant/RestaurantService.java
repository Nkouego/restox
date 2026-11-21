package com.laraim237.restox.restaurant;

import com.laraim237.restox.restaurant.dto.RestaurantDtos.CreateRestaurantRequest;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.RestaurantResponse;

public interface RestaurantService {
	
	RestaurantResponse createWithManager(CreateRestaurantRequest request);

}
