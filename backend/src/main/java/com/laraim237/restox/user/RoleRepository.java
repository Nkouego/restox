package com.laraim237.restox.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID>{

	Optional<Role> findByIdAndRestaurantId(UUID id, UUID restaurantId);
	
	List<Role> findAllByRestaurantId(UUID restaurantId);
	
}
