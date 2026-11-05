package com.laraim237.restox.auth;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, UUID>{

}
