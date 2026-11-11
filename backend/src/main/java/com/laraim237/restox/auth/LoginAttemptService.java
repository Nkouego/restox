package com.laraim237.restox.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.laraim237.restox.audit.AuditAction;
import com.laraim237.restox.audit.AuditService;
import com.laraim237.restox.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

	public final UserRepository userRepository;
	public final AuditService auditService;
	
	@Transactional
	public void recordFailure(String email) {
		userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
			int attempts = user.getFailedLoginAttempts() + 1;
			if(attempts >= 5) {
				user.setLockedUntil(Instant.now().plus(Duration.ofMinutes(15)));
				user.setFailedLoginAttempts(0);
				auditService.record(AuditAction.ACCOUNT_LOCKED, user.getId());
			} else {
				user.setFailedLoginAttempts(attempts);
				auditService.record(AuditAction.LOGIN_FAILURE, user.getId());
			}
		});	
	}
	
	@Transactional
	public void recordSuccess(UUID userId) {
		userRepository.findById(userId).ifPresent(user -> {
			user.setFailedLoginAttempts(0);
			user.setLockedUntil(null);
		});
	}
}
