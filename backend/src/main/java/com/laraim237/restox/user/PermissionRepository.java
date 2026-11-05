package com.laraim237.restox.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, UUID>{

	Optional<Permission> findByCode(String string);

}
