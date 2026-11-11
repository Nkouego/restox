package com.laraim237.restox.audit;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.laraim237.restox.user.User;
import com.laraim237.restox.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {
	
	public final AuditLogRepository auditLogRepository;
	
	public final UserRepository userRepository;

	public void record(AuditAction action, UUID actorId, UUID targetId) {
		record(action, actorId, targetId, null);
	}
	
	public void record(AuditAction action, UUID userId) {
		record(action, userId, userId);
	}
	
	public void record(AuditAction action, UUID actorId, UUID targetId, Map<String, Object> details) {
		User actor = actorId == null ? null : userRepository.getReferenceById(actorId);
		
		AuditLog auditLog = AuditLog.builder()
				.user(actor)
				.action(action.name())
				.targetType(action.getTargetType())
				.targetId(targetId)
				.details(details)
				.build();

		auditLogRepository.save(auditLog);
	}
	
	
}
