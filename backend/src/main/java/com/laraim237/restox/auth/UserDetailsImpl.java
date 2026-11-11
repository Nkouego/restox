package com.laraim237.restox.auth;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.laraim237.restox.user.User;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {
	
	private static final long serialVersionUID = 1L;
	
	private final User user;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Set.of();
	}

	@Override
	public @Nullable String getPassword() {
		return user.getPasswordHash();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return user.getLockedUntil() == null
				|| user.getLockedUntil().isBefore(Instant.now());
	}

	@Override
	public boolean isEnabled() {
		return user.isActive() 
				&& ( user.isPlatformAdmin() || user.getRestaurant().isActive());
	}
}
