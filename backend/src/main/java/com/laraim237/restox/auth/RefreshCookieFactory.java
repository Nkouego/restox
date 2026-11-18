package com.laraim237.restox.auth;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/*
 * @author Larissa Nkouego
 */

@Component
public class RefreshCookieFactory {

	private static final String SAME_SITE = "Strict";
	public static final String COOKIE_NAME = "restox_refresh";
	private static final String COOKIE_PATH = "/api/v1/auth";
	
	@Value("${app.security.refresh-token.cookie-secure}")
	private boolean secure;
	
	@Value("${app.security.refresh-token.ttl}")
	private Duration ttl;

	public ResponseCookie createCookie(String refreshToken) {
		return build(refreshToken, ttl);
	}
	
	public ResponseCookie clearCookie() {
		return build("", Duration.ZERO);
	}
	
	private ResponseCookie build(String value, Duration maxAge) {
		return ResponseCookie.from(COOKIE_NAME, value)
				.httpOnly(true)
				.secure(secure)
				.sameSite(SAME_SITE)
				.path(COOKIE_PATH)
				.maxAge(maxAge)
				.build();
	}
}
