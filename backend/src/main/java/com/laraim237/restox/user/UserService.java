package com.laraim237.restox.user;

import java.util.UUID;

import com.laraim237.restox.auth.dto.AuthDtos.CurrentUserResponse;

public interface UserService {
	CurrentUserResponse getCurrentUser(UUID userId);
}
