package com.laraim237.restox.restaurant;

import org.mapstruct.Mapper;

import com.laraim237.restox.restaurant.dto.RestaurantDtos.RestaurantResponse;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {
	
	RestaurantResponse toResponse(Restaurant restaurant);

}
