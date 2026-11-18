package com.laraim237.restox.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/*
 * @author Larissa Nkouego
 */
public interface TokenRepository extends JpaRepository<Token, UUID>{

	@EntityGraph(attributePaths = {"user", "user.restaurant"})
	Optional<Token> findByTokenHash(String tokenHash);
	
	@Modifying
	@Query("update Token t set t.used = true where t.id = :tokenId and t.used = false")
	int markAsUsed(UUID tokenId);
	
	@Modifying
	@Query("update Token t set t.revoked = true where t.familyId = :familyId")
	int revokeFamily(UUID familyId);
}
