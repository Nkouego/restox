package com.laraim237.restox.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PermissionRepository extends JpaRepository<Permission, UUID>{

	Optional<Permission> findByCode(String string);
	
	List<Permission> findByCodeStartingWith(String prefix);

	@Query("select p.code from User u join u.role r join r.permissions p where u.id = :userId")
	List<String> findCodesFromRole(UUID userId);
	
	@Query("select p.code from User u join u.directPermissions p where u.id = :userId")
	List<String> findCodesFromDirectPermissions(UUID userId);
	
	

}
