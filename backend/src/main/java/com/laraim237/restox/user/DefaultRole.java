package com.laraim237.restox.user;

import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DefaultRole {
	MANAGER(
			"Manager", Set.of(
					"MENU_VIEW", "MENU_MANAGE", "DISH_AVAILABILITY_UPDATE",
					"TABLE_VIEW", "TABLE_MANAGE", "STAFF_MANAGE",
					"ROLE_MANAGE", "PAYMENT_SETTINGS_MANAGE", "RESTAURANT_SETTINGS_MANAGE")
			),
	WAITER(
			"Serveur", Set.of(
					"MENU_VIEW", "TABLE_VIEW", "ORDER_CREATE", "BILL_REQUEST")
			),
	COOK(
			"Cuisinier", Set.of(
					"MENU_VIEW", "DISH_AVAILABILITY_UPDATE", "KITCHEN_VIEW", "ORDER_PREPARE")
			),
	CASHIER(
			"Caissier", Set.of(
					"TABLE_VIEW", "PAYMENT_COLLECT")
			);
	
	private final String roleName;
	private final Set<String> permissionCodes;
}
