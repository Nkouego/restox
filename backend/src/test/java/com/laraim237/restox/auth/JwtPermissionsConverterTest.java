package com.laraim237.restox.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import com.laraim237.restox.user.PermissionService;

@ExtendWith(MockitoExtension.class)
public class JwtPermissionsConverterTest {
	
	@Mock
	private PermissionService permissionService;

	@InjectMocks
	private JwtPermissionsConverter converter;
	
	@Test
	void shouldConvertPermissionCodesToAuthorithies() {
		UUID userId = UUID.randomUUID();
		when(permissionService.loadPermissionCodes(userId))
			.thenReturn(Set.of("ORDER_CREATE", "MENU_READ"));
		
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "HS256")
				.subject(userId.toString())
				.build();
		
		AbstractAuthenticationToken results = converter.convert(jwt);
		
		assertThat(results.getAuthorities())
		.extracting(GrantedAuthority::getAuthority)
		.containsExactlyInAnyOrder("ORDER_CREATE", "MENU_READ");				
	}
}
