package com.laraim237.restox.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.laraim237.restox.audit.AuditLog;
import com.laraim237.restox.audit.AuditLogRepository;
import com.laraim237.restox.restaurant.Restaurant;
import com.laraim237.restox.restaurant.RestaurantRepository;
import com.laraim237.restox.user.Permission;
import com.laraim237.restox.user.PermissionRepository;
import com.laraim237.restox.user.Role;
import com.laraim237.restox.user.RoleRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Testcontainers
@Transactional
public class PersistenceIntegrationTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

	@Autowired
	RestaurantRepository restaurantRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PermissionRepository permissionRepository;

	@Autowired
	AuditLogRepository auditLogRepository;

	@Autowired
	EntityManager entityManager;

	@Test
	void shouldSaveAndReloadRestaurant() {

		Restaurant restaurant = new Restaurant();
		restaurant.setName("Nom du restaurant");

		restaurantRepository.save(restaurant);
		entityManager.flush();
		entityManager.clear();

		Restaurant found = restaurantRepository.findById(restaurant.getId()).orElseThrow();
		assertThat(found.getName()).isEqualTo("Nom du restaurant");
		assertThat(found.isActive()).isTrue();
		assertThat(found.getCreatedAt()).isNotNull();
		assertThat(found.getUpdatedAt()).isNotNull();

	}

	@Test
	void shouldSaveRoleWithPermissionAndReloadIt() {

		Restaurant restaurant = new Restaurant();
		restaurant.setName("test resto");
		restaurant = restaurantRepository.save(restaurant);

		Permission menuView = permissionRepository.findByCode("MENU_VIEW").orElseThrow();

		Role role = new Role();
		role.setName("Serveur");
		role.setRestaurant(restaurant);
		role.getPermissions().add(menuView);

		roleRepository.save(role);

		entityManager.flush();
		entityManager.clear();

		Role found = roleRepository.findById(role.getId()).orElseThrow();
		assertThat(found.getPermissions()).hasSize(1);
		assertThat(found.getPermissions().iterator().next().getCode()).isEqualTo("MENU_VIEW");

	}

	@Test
	void shouldSaveAuditLogWithJsonDetails() {

		AuditLog auditLog = new AuditLog();

		Map<String, Object> details = Map.of("restaurantName", "Resto du test", "numberOfTables", 12, "active", true,
				"restaurantId", UUID.randomUUID());

		auditLog.setDetails(details);
		auditLog.setAction("RESTAURANT_CREATED");
		auditLog.setTargetType("RESTAURANT");

		auditLogRepository.save(auditLog);

		entityManager.flush();
		entityManager.clear();

		AuditLog found = auditLogRepository.findById(auditLog.getId()).orElseThrow();
		assertThat(found.getDetails()).hasSize(4);
		assertThat(found.getCreatedAt()).isNotNull();
		assertThat(found.getTargetType()).isEqualTo("RESTAURANT");

	}
}
