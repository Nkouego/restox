package com.laraim237.restox.auth;

import java.util.List;
import java.util.UUID;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.laraim237.restox.user.PermissionService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtPermissionsConverter implements Converter<Jwt, AbstractAuthenticationToken>{
	
	private final PermissionService permissionService;

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		UUID userId = UUID.fromString(jwt.getSubject());
		List<SimpleGrantedAuthority> authorities = permissionService.loadPermissionCodes(userId).stream()
				.map(SimpleGrantedAuthority::new)
				.toList();
		
		return new JwtAuthenticationToken(jwt, authorities);
	}
	
}
