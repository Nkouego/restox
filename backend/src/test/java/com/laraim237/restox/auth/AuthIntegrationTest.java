package com.laraim237.restox.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.jayway.jsonpath.JsonPath;
import com.laraim237.restox.audit.AuditLogRepository;
import com.laraim237.restox.user.PermissionRepository;
import com.laraim237.restox.user.User;
import com.laraim237.restox.user.UserRepository;

import jakarta.servlet.http.Cookie;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

	private static final String EMAIL = "admin@test.local";
	private static final String PASSWORD = "correct-password-123";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AuditLogRepository auditLogRepository;
	@Autowired
	private TokenRepository tokenRepository;
	@Autowired
	private PermissionRepository permissionRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		auditLogRepository.deleteAll();
		tokenRepository.deleteAll();
		userRepository.deleteAll();
		userRepository.save(User.builder().email(EMAIL).fullName("Test Admin")
				.passwordHash(passwordEncoder.encode(PASSWORD)).platformAdmin(true).active(true)
				.directPermissions(new HashSet<>(permissionRepository.findByCodeStartingWith("PLATFORM_"))).build());
	}

	/*
	 * Test de connection
	 */

	private ResultActions login(String email, String password) throws Exception {
		return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
				{"email": "%s", "password": "%s"}
				""".formatted(email, password)));
	}

	@Test
	void shouldLoginWithValidCredentials() throws Exception {
		login(EMAIL, PASSWORD).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer")).andExpect(jsonPath("$.expiresIn").value(900));
	}

	@Test
	void shouldReturnSameErrorForUnknownEmailAndInvalidPassword() throws Exception {
		login("wrongadmin@test.local", PASSWORD).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

		login(EMAIL, "123").andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void shouldLockAccountAfterFiveFailedAttempts() throws Exception {
		for (int i = 0; i < 5; i++) {
			login(EMAIL, "wrong-password").andExpect(status().isUnauthorized());
		}

		login(EMAIL, PASSWORD).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
	}

	@Test
	void shouldRejectDisabledUser() throws Exception {

		User user = userRepository.findByEmailIgnoreCase(EMAIL).orElseThrow();
		user.setActive(false);
		userRepository.save(user);

		login(EMAIL, PASSWORD).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCOUNT_DISABLED"));
	}

	@Test
	void shouldRejectInvalidBody() throws Exception {
		login("", PASSWORD).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").exists());
	}

	@Test
	void shouldRejectMeWithoutToken() throws Exception {
		mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
	}

	@Test
	void shouldRejectMeWithForgetToken() throws Exception {
		mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer abc.def.ghi"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldReturnCurrentUserWithPermissions() throws Exception {

		String body = login(EMAIL, PASSWORD).andReturn().getResponse().getContentAsString();

		String token = JsonPath.read(body, "$.accessToken");

		mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(EMAIL)).andExpect(jsonPath("$.platformAdmin").value(true))
				.andExpect(jsonPath("$.restaurant").isEmpty()).andExpect(jsonPath("$.permissions").isNotEmpty());
	}

	/*
	 * Test de refresh token
	 */

	@Test
	void shouldSetSecureRefreshCookieOnLogin() throws Exception {
		login(EMAIL, PASSWORD).andExpect(status().isOk()).andExpect(cookie().exists("restox_refresh"))
				.andExpect(cookie().httpOnly("restox_refresh", true))
				.andExpect(cookie().path("restox_refresh", "/api/v1/auth"));
	}

	@Test
	void shouldRefreshAndRotateToken() throws Exception {
		Cookie oldCookie = loginAndGetRefreshCookie();

		Cookie newCookie = mockMvc.perform(post("/api/v1/auth/refresh").cookie(oldCookie))
				.andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer")).andReturn().getResponse()
				.getCookie("restox_refresh");

		assertThat(newCookie).isNotNull();

		assertThat(newCookie.getValue()).isNotEqualTo(oldCookie.getValue());

	}
	
	@Test
	void shouldRejectReusedRefreshTokenAndRevokeFamily() throws Exception {
		Cookie first = loginAndGetRefreshCookie();

		Cookie second = mockMvc.perform(post("/api/v1/auth/refresh").cookie(first))
				.andExpect(status().isOk())
				.andReturn().getResponse().getCookie("restox_refresh");
		
		assertThat(second).isNotNull();

		mockMvc.perform(post("/api/v1/auth/refresh").cookie(first))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/auth/refresh").cookie(second))
				.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldRejectRefreshWithoutOrWithInvalidCookie() throws Exception {
		// 1. Aucun cookie envoyé
	    mockMvc.perform(post("/api/v1/auth/refresh"))
	        .andExpect(status().isUnauthorized());

	    // 2. Cookie invalide envoyé
	    mockMvc.perform(post("/api/v1/auth/refresh")
	            .cookie(new Cookie("restox_refresh", "bad-token")))
	        .andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldRevokeRefreshTokenOnLogout() throws Exception {
		Cookie oldCookie = loginAndGetRefreshCookie();
		
		Cookie clearedCookie = mockMvc.perform(post("/api/v1/auth/logout").cookie(oldCookie))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getCookie("restox_refresh");
		
		assertThat(clearedCookie).isNotNull();
		assertThat(clearedCookie.getMaxAge()).isZero();
		
		mockMvc.perform(post("/api/v1/auth/refresh").cookie(oldCookie))
        .andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldRejectRefreshForDisabledUser() throws Exception{
		Cookie refreshCookie = loginAndGetRefreshCookie();
		 
		User user = userRepository.findByEmailIgnoreCase(EMAIL)
				.orElseThrow();
	
		user.setActive(false);
		userRepository.save(user);
		
		mockMvc.perform(post("/api/v1/auth/refresh")
		       .cookie(refreshCookie))
		       .andExpect(status().isUnauthorized());
	}
	
	private Cookie loginAndGetRefreshCookie() throws Exception {
		return login(EMAIL, PASSWORD).andExpect(status().isOk()).andReturn().getResponse().getCookie("restox_refresh");
	}
}