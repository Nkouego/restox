package com.laraim237.restox.user;

import java.time.Instant;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "users")
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(name = "full_name", nullable = false, length = 150)
	private String fullName;
	
	@Column(nullable = false, length = 255) 
	private String email;
	
	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;
	
	@Column(name = "photo_url", columnDefinition = "TEXT")
	private String photoUrl;
	
	@Column(name = "is_platform_admin", nullable = false)
	private boolean platformAdmin;
	
	@Builder.Default
	@Column(nullable = false)
	private boolean active = true;
	
	@Builder.Default
	@Column(name = "failed_login_attempts", nullable = false)
	private int failedLoginAttempts = 0;
	
	@Column(name = "locked_until")
	private Instant lockedUntil;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "restaurant_id")
	private Restaurant restaurant;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id")
	private Role role;
	
	@Builder.Default
	@ManyToMany
	@JoinTable(
	    name = "user_permission",
	    joinColumns = @JoinColumn(name = "user_id"),
	    inverseJoinColumns = @JoinColumn(name = "permission_id")
	)
	private Set<Permission> directPermissions = new HashSet<>();
	
	@Column(name = "created_at", updatable = false) 
	@CreationTimestamp
	private Instant createdAt;
	
	@Column(name = "updated_at")
	@UpdateTimestamp
	private Instant updatedAt;
	
}
