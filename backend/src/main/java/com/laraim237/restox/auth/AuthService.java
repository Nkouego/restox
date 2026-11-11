package com.laraim237.restox.auth;

import com.laraim237.restox.auth.dto.AuthDtos.LoginRequest;
import com.laraim237.restox.auth.dto.AuthDtos.LoginResponse;

public interface AuthService {
	
	public LoginResponse login(LoginRequest request);

}
