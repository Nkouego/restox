package com.laraim237.restox.auth;

import com.laraim237.restox.auth.dto.AuthDtos.AuthResponse;
import com.laraim237.restox.auth.dto.AuthDtos.LoginRequest;

public interface AuthService {
	
	AuthResponse login(LoginRequest request);
	
	AuthResponse refresh(String refreshToken);

	void logout(String refreshToken);

}
