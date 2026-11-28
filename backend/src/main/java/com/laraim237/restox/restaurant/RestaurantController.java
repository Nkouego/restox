package com.laraim237.restox.restaurant;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.laraim237.restox.restaurant.dto.RestaurantDtos.RestaurantFilterRequest;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.RestaurantResponse;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.UpdateRestaurantRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class RestaurantController {
	private final RestaurantService restaurantService;
	
	@GetMapping("/admin/restaurants")
	@PreAuthorize("hasAuthority('PLATFORM_RESTAURANT_VIEW')")
	public Page<RestaurantResponse> list(
			@Valid RestaurantFilterRequest request,
			@PageableDefault(size=20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
			){
		return restaurantService.list(request, pageable);
	}
	
	@GetMapping("/admin/restaurants/{restaurantId}")
	@PreAuthorize("hasAuthority('PLATFORM_RESTAURANT_VIEW')")
	public RestaurantResponse getById(@PathVariable UUID restaurantId) {
		return restaurantService.getById(restaurantId);
	}
	
	@PatchMapping("/restaurant/me")
	@PreAuthorize("hasAuthority('RESTAURANT_SETTINGS_MANAGE')")
	public RestaurantResponse update(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody UpdateRestaurantRequest request) {
		UUID restaurantId = UUID.fromString(jwt.getClaimAsString("restaurantId"));
		return restaurantService.update(restaurantId, request, actorId(jwt));
		
	}
	
	@PostMapping("/admin/restaurants/{restaurantId}/deactivate")
	@PreAuthorize("hasAuthority('PLATFORM_RESTAURANT_DISABLE')")
	public ResponseEntity<Void> deactivate(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID restaurantId) {
		restaurantService.deactivate(restaurantId, actorId(jwt));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/admin/restaurants/{restaurantId}/activate")
	@PreAuthorize("hasAuthority('PLATFORM_RESTAURANT_REACTIVATE')")
	public ResponseEntity<Void> activate(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID restaurantId) {
		restaurantService.activate(restaurantId, actorId(jwt));
		return ResponseEntity.noContent().build();
	}
	
	private UUID actorId(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}
}
