package com.laraim237.restox.user;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.laraim237.restox.auth.dto.AuthDtos.CurrentUserResponse;
import com.laraim237.restox.restaurant.Restaurant;
import com.laraim237.restox.restaurant.dto.RestaurantSummary;

@Mapper(componentModel = "Spring")
public interface UserMapper {
	
	@Mapping(target = "id", source = "user.id")
	@Mapping(target = "restaurant", source = "user.restaurant")
	@Mapping(target = "permissions", source = "permissions")
	CurrentUserResponse toResponse(User user, Set<String> permissions);
	
	RestaurantSummary summary(Restaurant restaurant);
}
