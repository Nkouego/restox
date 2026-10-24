# RestoX

[![CI](https://github.com/Nkouego/restox/actions/workflows/ci.yml/badge.svg)](https://github.com/Nkouego/restox/actions/workflows/ci.yml)

Plateforme web multi-restaurants (SaaS) qui fait circuler une commande de la salle à la cuisine, puis à la caisse, avec un statut visible par tous les acteurs.

> **État du projet : fondations en cours.** Le dépôt contient pour l'instant l'infrastructure de base (backend, frontend, base de données, CI). Les fonctionnalités métier arrivent ensuite, par tranches.

## Le problème résolu

Quand les commandes passent sur papier ou à l'oral entre la salle, la cuisine et la caisse, elles sont oubliées, mal transmises ou servies en retard. RestoX centralise ce flux et affiche l'état de chaque commande en temps utile.

L'application s'adresse au personnel du restaurant (manager, serveur, cuisinier, caissier), pas au client final. Chaque restaurant dispose d'un espace isolé, créé et administré par l'équipe de la plateforme.

## Fonctionnalités prévues (MVP)

- Authentification et gestion des restaurants par l'administrateur de la plateforme
- Gestion du personnel, du menu et des tables
- Prise de commande par le serveur
- Écran cuisine avec les statuts : reçue, en préparation, prête
- Demande d'addition et encaissement par le caissier (espèces, puis Orange Money)

## Stack technique

| Couche | Technologies |
|---|---|
| Backend | Java 21, Spring Boot, Spring Data JPA, Maven |
| Base de données | PostgreSQL, migrations Flyway |
| Frontend | React, TypeScript, Vite, Tailwind CSS |
| Qualité | ESLint, Prettier, tests avec Testcontainers |
| Intégration continue | GitHub Actions |
| Développement local | Docker Compose (PostgreSQL, Mailpit) |

## Structure du dépôt

```
restox/
├── backend/             # API Spring Boot
├── frontend/            # Application React
├── docker-compose.yml   # PostgreSQL et Mailpit pour le développement
├── .env.example         # Modèle des variables d'environnement
└── .github/workflows/   # Pipeline CI
```

## Prérequis

- JDK 21
- Maven 3.9 ou plus
- Node.js (version LTS) et npm
- Docker avec Docker Compose

## Démarrage rapide

1. **Cloner le dépôt**

   ```bash
   git clone https://github.com/Nkouego/restox.git
   cd restox
   ```

2. **Configurer les variables d'environnement**

   Copie `.env.example` en `.env` et renseigne les valeurs. Le fichier `.env` est ignoré par Git : ne le publie jamais.

3. **Démarrer la base de données et Mailpit**

   ```bash
   docker compose up -d
   ```

4. **Lancer le backend** (dans un premier terminal)

   ```bash
   cd backend
   mvn spring-boot:run
   ```

5. **Lancer le frontend** (dans un second terminal)

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

| Service | Adresse |
|---|---|
| Frontend | http://localhost:5173 |
| API (test) | http://localhost:8080/api/v1/ping |
| Santé du backend | http://localhost:8080/actuator/health |
| Mailpit (e-mails de test) | http://localhost:8025 |

## Tests et qualité

Les tests du backend utilisent Testcontainers : Docker doit être démarré, mais aucune base locale n'est nécessaire.

```bash
# Backend
cd backend
mvn -ntp verify

# Frontend
cd frontend
npm run lint
npm run format:check
npm run build
```

Ces mêmes vérifications sont exécutées automatiquement par la CI à chaque push et chaque pull request.

## Auteur

Larissa, [@Nkouego](https://github.com/Nkouego) 