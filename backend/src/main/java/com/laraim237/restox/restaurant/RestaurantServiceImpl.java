package com.laraim237.restox.restaurant;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.laraim237.restox.audit.AuditService;
import com.laraim237.restox.auth.TokenUtils;
import com.laraim237.restox.common.exception.ConflictException;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.CreateRestaurantRequest;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.RestaurantResponse;
import com.laraim237.restox.user.DefaultRole;
import com.laraim237.restox.user.Permission;
import com.laraim237.restox.user.PermissionRepository;
import com.laraim237.restox.user.Role;
import com.laraim237.restox.user.RoleRepository;
import com.laraim237.restox.user.User;
import com.laraim237.restox.user.UserRepository;

import lombok.RequiredArgsConstructor;

/*
 * @author Larissa Nkouego
 */
@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

	private final AuditService auditService;
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RestaurantMapper restaurantMapper;
	private final RestaurantRepository restaurantRepository;
	private final PermissionRepository permissionRepository;

	
	/*
	 * Crée un restaurant avec ses 4 rôles par défaut et son premier manager
	 */
	@Override
	@Transactional
	public RestaurantResponse createWithManager(CreateRestaurantRequest request) {
		String email = request.managerEmail().trim();
		
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new ConflictException("EMAIL_ALREADY_USED", "Email already used");
		}
		
		Restaurant restaurant = Restaurant.builder()
				.name(request.name())
				.description(request.description())
				.build();
		restaurant = restaurantRepository.save(restaurant);
		
		Role managerRole = null;
		
		for(DefaultRole defaultRole: DefaultRole.values()) {
			Set<Permission> permissions = new HashSet<>(permissionRepository.findByCodeIn(defaultRole.getPermissionCodes()));
			
			if (permissions.size() != defaultRole.getPermissionCodes().size()) {
				throw new IllegalStateException("Unknown permission code in default role " + defaultRole);
			}
			
			Role role = roleRepository.save(Role.builder()
					.name(defaultRole.getRoleName())
					.permissions(permissions)
					.restaurant(restaurant)
					.build());
			
			if(defaultRole == DefaultRole.MANAGER) {
				managerRole = role;
			}
		}
		
		User user = User.builder()
				.fullName(request.managerFullName())
				.email(email)
				.passwordHash(passwordEncoder.encode(TokenUtils.generate()))
				.role(managerRole)
				.platformAdmin(false)
				.restaurant(restaurant)
				.build();
		
		userRepository.save(user);
		
		return restaurantMapper.toResponse(restaurant); 
	}

}
