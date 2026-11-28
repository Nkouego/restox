package com.laraim237.restox.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.laraim237.restox.common.exception.ConflictException;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.CreateRestaurantRequest;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.RestaurantFilterRequest;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.RestaurantResponse;
import com.laraim237.restox.user.PermissionRepository;
import com.laraim237.restox.user.Role;
import com.laraim237.restox.user.RoleRepository;
import com.laraim237.restox.user.User;
import com.laraim237.restox.user.UserRepository;

@SpringBootTest
@Testcontainers
public class RestaurantServiceIntegrationTest {
	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
	
	@Autowired RoleRepository roleRepository;
	@Autowired UserRepository userRepository;
	@Autowired PermissionRepository permissionRepository;
	@Autowired RestaurantService restaurantService;
	@Autowired RestaurantRepository restaurantRepository;
	
	private CreateRestaurantRequest request(String email) {
		return new CreateRestaurantRequest("Chez Mama", "Cuisine camerounaise", "Mama Africa", email);
	}
	
	@Test
	void createWithManager_createsActiveRestaurant() {
		String email = "manager-" + UUID.randomUUID() + "@test.com";

		RestaurantResponse response = restaurantService.createWithManager(request(email));

		assertThat(response.id()).isNotNull();
		assertThat(response.name()).isEqualTo("Chez Mama");
		assertThat(response.active()).isTrue();
		
		Optional<Restaurant> restaurant = restaurantRepository.findById(response.id());
		
		assertThat(restaurant).isPresent();
		assertThat(restaurant.get().isActive()).isTrue();
	}
	
	@Test
	void createWithManager_createsFourDefaultRoles(){
		String email = "manager-" + UUID.randomUUID() + "@test.com";

		RestaurantResponse response = restaurantService.createWithManager(request(email));
		
		List<Role> roles = roleRepository.findAllByRestaurantId(response.id());
		
		assertThat(roles)
		.extracting(Role::getName)
		.containsExactlyInAnyOrder("Manager", "Serveur", "Cuisinier", "Caissier");
	}
	
	@Test
	void createWithManager_grantsManagerPermissionsOnly() {
		String email = "manager-" + UUID.randomUUID() + "@test.com";
		
		restaurantService.createWithManager(request(email));

		User manager = userRepository.findByEmailIgnoreCase(email).orElseThrow();
		
		var codes = permissionRepository.findCodesFromRole(manager.getId());

		assertThat(codes).hasSize(9);
		assertThat(codes).noneMatch(code -> code.startsWith("PLATFORM_"));
	}
	
	@Test
	void createWithManager_rejectsDuplicateEmail() {
		String email = "manager-" + UUID.randomUUID() + "@test.com";
		
		restaurantService.createWithManager(request(email));

		assertThatThrownBy(() -> restaurantService.createWithManager(request(email)))
				.isInstanceOf(ConflictException.class);
	}
	
	@Test
	void list_filtersByNameAndActive() {
		 String token = UUID.randomUUID().toString().substring(0, 8);
		 
		 restaurantRepository.save(Restaurant.builder().name("Alpha " + token).active(true).build());
		 restaurantRepository.save(Restaurant.builder().name("Beta " + token).active(true).build());
		 restaurantRepository.save(Restaurant.builder().name("Gamma " + token).active(false).build());
		 
		 Pageable page = PageRequest.of(0, 10);
		 
		 assertThat(restaurantService.list(new RestaurantFilterRequest(token, null), page).getTotalElements()).isEqualTo(3);
		 assertThat(restaurantService.list(new RestaurantFilterRequest(token, true), page).getTotalElements()).isEqualTo(2);
		 assertThat(restaurantService.list(new RestaurantFilterRequest(token, false), page).getTotalElements()).isEqualTo(1);
	}
}