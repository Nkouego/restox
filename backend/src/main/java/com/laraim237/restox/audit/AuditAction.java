package com.laraim237.restox.audit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditAction {
	LOGIN_SUCCESS("User"),
	LOGIN_FAILURE("User"),
	ACCOUNT_LOCKED("User");
	
	public final String targetType;
}