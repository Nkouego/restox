package com.laraim237.restox.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByEmailIgnoreCase(String email);

	@Query("""
			    SELECT u FROM User u
			    LEFT JOIN FETCH u.restaurant
			    WHERE LOWER(u.email) = LOWER(:email)
			""")
	Optional<User> findByEmailWithRestaurant(String email);

	boolean existsByPlatformAdminTrue();
	
	@EntityGraph(attributePaths = "restaurant")
	Optional<User> findWithRestaurantById(UUID userId);

	boolean existsByEmailIgnoreCase(String managerEmail);
}
