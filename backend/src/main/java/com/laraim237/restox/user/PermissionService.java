package com.laraim237.restox.user;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {
	
	private final UserRepository userRepository;
	private final PermissionRepository permissionRepository;

	@Cacheable(cacheNames = "permissions", key = "#userId")
	public Set<String> loadPermissionCodes(UUID userId) {
		User user = userRepository.findWithRestaurantById(userId).orElse(null);

		if (user == null
				|| !user.isActive()
				|| (!user.isPlatformAdmin() && !user.getRestaurant().isActive())) {
			return Set.of();
		}

		Set<String> codes = new HashSet<>(permissionRepository.findCodesFromRole(userId));
		codes.addAll(permissionRepository.findCodesFromDirectPermissions(userId));
		return Set.copyOf(codes);
	}
}
