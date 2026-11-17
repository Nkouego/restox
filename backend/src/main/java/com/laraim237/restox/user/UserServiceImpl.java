package com.laraim237.restox.user;

import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.laraim237.restox.auth.dto.AuthDtos.CurrentUserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;
	private final UserRepository userRepository;
	private final PermissionService permissionService;
	
	@Override
	@Transactional(readOnly=true)
	public CurrentUserResponse getCurrentUser(UUID userId) {
		
		User user = userRepository.findWithRestaurantById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		
		return userMapper.toResponse(user, permissionService.loadPermissionCodes(userId));
		
	}

}
