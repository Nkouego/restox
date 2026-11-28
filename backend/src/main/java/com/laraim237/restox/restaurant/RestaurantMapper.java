package com.laraim237.restox.restaurant;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.laraim237.restox.restaurant.dto.RestaurantDtos.RestaurantResponse;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.UpdateRestaurantRequest;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateEntity(UpdateRestaurantRequest request, @MappingTarget Restaurant restaurant);
	
	RestaurantResponse toResponse(Restaurant restaurant);

}
