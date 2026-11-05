package com.laraim237.restox.user;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.laraim237.restox.restaurant.Restaurant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role")
public class Role {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(nullable = false, length = 100 )
	private String name;
	
	@Column(length = 255)
	private String description;
	
	@Builder.Default
	@Column(name = "is_default", nullable = false)
	private boolean defaultRole = false;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="restaurant_id", nullable = false)
	private Restaurant restaurant;
	
	@ManyToMany
	@JoinTable(
			name = "role_permission",
			joinColumns = @JoinColumn(name = "role_id"),
			inverseJoinColumns =  @JoinColumn(name = "permission_id")
	)
	@Builder.Default
	private Set<Permission> permissions = new HashSet<>();

}
