package com.laraim237.restox.audit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditAction {
	LOGIN_SUCCESS("User"),
	LOGIN_FAILURE("User"),
	ACCOUNT_LOCKED("User"), 
	RESTAURANT_UPDATED("Restaurant"),
	RESTAURANT_DEACTIVATED("Restaurant"),
	RESTAURANT_REACTIVATED("Restaurant");
	
	public final String targetType;
}