package com.laraim237.restox.restaurant;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "restaurant")
public class Restaurant {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(nullable = false)
	private String name;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	@Builder.Default
	@Column(nullable = false)
	private boolean active = true;
	
	@Column(name = "photo_url", columnDefinition = "TEXT")
	private String photoUrl;
	
	@Column(name = "created_at", updatable = false)
	@CreationTimestamp
	private Instant createdAt;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	private Instant updatedAt;
	
}
