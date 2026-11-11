package com.laraim237.restox.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

	private static final int MIN_PASSWORD_LENGTH = 12;
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.bootstrap.admin.email:}")
	public String adminEmail;

	@Value("${app.bootstrap.admin.password:}")
	public String adminPassword;

	@Value("${app.bootstrap.admin.full-name:Platform Admin}")
	public String adminFullName;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {

		// 1. Vérifier la présence des variables d'environnement
		if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {

			log.warn("Création de l'administrateur ignorée : " + "ADMIN_EMAIL ou ADMIN_PASSWORD est manquant.");
			return;
		}

		// 2. Vérifier si un administrateur existe déjà
		if (userRepository.existsByPlatformAdminTrue()) {
			log.info("Un administrateur de plateforme existe déjà. " + "Aucune création nécessaire.");
			return;
		}
		
		// 3. Vérifier la longueur minimale du mot de passe
        if (adminPassword.length() < MIN_PASSWORD_LENGTH) {
            log.error(
                "Création de l'administrateur impossible : "
                + "le mot de passe doit contenir au moins {} caractères.",
                MIN_PASSWORD_LENGTH
            );
            return;
        }

		User admin = User.builder()
				.email(adminEmail.trim())
				.fullName(adminFullName)
				.passwordHash(passwordEncoder.encode(adminPassword))
				.restaurant(null)
				.role(null)
				.platformAdmin(true)
				.active(true)
				.build();

		userRepository.save(admin);

		log.info("Administrateur de plateforme créé avec succès : ");
	}
}
