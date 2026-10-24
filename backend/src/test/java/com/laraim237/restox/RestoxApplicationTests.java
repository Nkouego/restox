package com.laraim237.restox;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
class RestoxApplicationTests {
	
	@ServiceConnection
	@Container
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

	@Test
	void contextLoads() {
	}

}
