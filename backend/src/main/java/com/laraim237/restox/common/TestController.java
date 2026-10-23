package com.laraim237.restox.common;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	
	@GetMapping("/api/v1/test")
	public Map<String, String> test(){
		return Map.of("message","connexion backend-frontend reussie!");
	}

}
