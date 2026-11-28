package com.laraim237.restox.restaurant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.laraim237.restox.audit.AuditAction;
import com.laraim237.restox.audit.AuditService;
import com.laraim237.restox.auth.TokenUtils;
import com.laraim237.restox.common.exception.ConflictException;
import com.laraim237.restox.common.exception.NotFoundException;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.CreateRestaurantRequest;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.RestaurantFilterRequest;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.RestaurantResponse;
import com.laraim237.restox.restaurant.dto.RestaurantDtos.UpdateRestaurantRequest;
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


	@Override
	@Transactional(readOnly = true)
	public Page<RestaurantResponse> list( RestaurantFilterRequest request ,Pageable pageable) {
		
		List<Specification<Restaurant>> specs = new ArrayList<>();
		
		if(StringUtils.hasText(request.name())) {
			specs.add(RestaurantSpecifications.containsName(request.name().trim()));
		}
		
		if(request.active() != null) {
			specs.add(RestaurantSpecifications.hasActive(request.active()));
		}
		
		return restaurantRepository.findAll(Specification.allOf(specs), pageable)
				.map(restaurantMapper::toResponse);
	}
	
	@Override
	@Transactional(readOnly = true)
	public RestaurantResponse getById(UUID restaurantId) {
		
		Restaurant restaurant = findOrThrow(restaurantId);
		
		return restaurantMapper.toResponse(restaurant);
	}


	@Override
	@Transactional
	public RestaurantResponse update(UUID restaurantId, UpdateRestaurantRequest request, UUID actorId) {
		
		Restaurant restaurant = findOrThrow(restaurantId);
		
		restaurantMapper.updateEntity(request, restaurant);
		
		auditService.record(AuditAction.RESTAURANT_UPDATED, actorId, restaurantId);
		
		return restaurantMapper.toResponse(restaurant);
	}


	@Override
	@Transactional
	public void activate(UUID restaurantId, UUID actorId) {
		Restaurant restaurant = findOrThrow(restaurantId);
		
		if (restaurant.isActive()) {
			throw new ConflictException("RESTAURANT_ALREADY_ACTIVE", "Restaurant already active");
		}
		
		restaurant.setActive(true);
		
		auditService.record(AuditAction.RESTAURANT_REACTIVATED, actorId, restaurantId);
	}

	@Override
	@Transactional
	@CacheEvict(value = "permissions", allEntries = true)
	public void deactivate(UUID restaurantId, UUID actorId) {
		
		Restaurant restaurant = findOrThrow(restaurantId);
		
		if (!restaurant.isActive()) {
			throw new ConflictException("RESTAURANT_ALREADY_INACTIVE", "Restaurant already inactive");
		}
		
		restaurant.setActive(false);
		
		auditService.record(AuditAction.RESTAURANT_DEACTIVATED, actorId, restaurantId);
	}
	
	private Restaurant findOrThrow(UUID restaurantId) {
		return restaurantRepository.findById(restaurantId)
				.orElseThrow(() -> new NotFoundException("RESTAURANT_NOT_FOUND", "Restaurant not found"));
	}

}
