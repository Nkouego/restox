package com.laraim237.restox.restaurant;

import org.springframework.data.jpa.domain.Specification;

public class RestaurantSpecifications {
	
	private RestaurantSpecifications() {
	}
	
	public static Specification<Restaurant> hasActive(boolean active){
		return (root, query, cb) -> cb.equal(root.get("active"), active);
	}

	public static Specification<Restaurant> containsName(String name){
		return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" +name.toLowerCase()+ "%");
	}
}
