package com.laraim237.restox.restaurant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.laraim237.restox.auth.JwtService;
import com.laraim237.restox.auth.UserDetailsImpl;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.CreateRestaurantRequest;
import com.laraim237.restox.user.User;
import com.laraim237.restox.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class RestaurantControllerTest {
	
	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

	@Autowired MockMvc mockMvc;
	@Autowired RestaurantRepository restaurantRepository;
	@Autowired JwtService jwtService;
	@Autowired RestaurantService restaurantService;
	@Autowired UserRepository userRepository;
	
	@Test
	void list_requiresPermission() throws Exception {
		mockMvc.perform(get("/api/v1/admin/restaurants"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/v1/admin/restaurants")
				.with(jwt().authorities(new SimpleGrantedAuthority("MENU_VIEW"))))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/v1/admin/restaurants")
				.with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_RESTAURANT_VIEW"))))
				.andExpect(status().isOk());
	}
	
	@Test
	void rejectWrongRestaurantId() throws Exception {
		mockMvc.perform(get("/api/v1/admin/restaurants/"+UUID.randomUUID())
				.with(jwt().authorities(new SimpleGrantedAuthority("PLATFORM_RESTAURANT_VIEW")))
				)
		.andExpect(status().isNotFound())
		.andExpect(jsonPath("$.code").value("RESTAURANT_NOT_FOUND"));
	}
	
	@Test
	void updateMine_changesOwnRestaurant() throws Exception {
		String email = "manager-" + UUID.randomUUID() + "@test.com";
		UUID managerId = createManager(email);
		UUID restaurantId = userRepository.findWithRestaurantById(managerId).orElseThrow()
				.getRestaurant().getId();

		mockMvc.perform(patch("/api/v1/restaurant/me")
				.with(jwt()
						.jwt(j -> j.subject(managerId.toString()).claim("restaurantId", restaurantId.toString()))
						.authorities(new SimpleGrantedAuthority("RESTAURANT_SETTINGS_MANAGE")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"New name\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("New name"));
	}
	
	private UUID createManager(String email) {
		restaurantService.createWithManager(
				new CreateRestaurantRequest("Old name", "desc", "Manager Test", email));
		return userRepository.findByEmailIgnoreCase(email).orElseThrow().getId();
	}
	
	@Test
	void deactivate_thenConflictWhenAlreadyInactive() throws Exception {
		String email = "manager-" + UUID.randomUUID() + "@test.com";
		UUID managerId = createManager(email);
		UUID restaurantId = userRepository.findWithRestaurantById(managerId).orElseThrow()
				.getRestaurant().getId();

		var admin = jwt()
				.jwt(j -> j.subject(managerId.toString()))
				.authorities(new SimpleGrantedAuthority("PLATFORM_RESTAURANT_DISABLE"));

		mockMvc.perform(post("/api/v1/admin/restaurants/" + restaurantId + "/deactivate").with(admin))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/admin/restaurants/" + restaurantId + "/deactivate").with(admin))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("RESTAURANT_ALREADY_INACTIVE"));
	}
	
	@Test
	void userLosesAccess_afterRestaurantDeactivation() throws Exception {
	    UUID managerId = createManager("manager-" + UUID.randomUUID() + "@test.com");
	    UUID restaurantId = userRepository.findWithRestaurantById(managerId).orElseThrow()
				.getRestaurant().getId();
	    User manager = userRepository.findWithRestaurantById(managerId).orElseThrow();
	    String token = jwtService.generateAccessToken(new UserDetailsImpl(manager));

	    mockMvc.perform(patch("/api/v1/restaurant/me")
	            .header("Authorization", "Bearer " + token)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"name\":\"A\"}"))
	            .andExpect(status().isOk());

	    restaurantService.deactivate(restaurantId, managerId);

	    mockMvc.perform(patch("/api/v1/restaurant/me")
	            .header("Authorization", "Bearer " + token)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"name\":\"B\"}"))
	            .andExpect(status().isForbidden());
	}
	
	

}
