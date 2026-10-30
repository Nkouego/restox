-- V2__create_auth_schema.sql
-- Schéma initial : restaurants, permissions, rôles, utilisateurs,
-- jetons d'authentification et journal d'audit.

-- =========================================================
-- 1. RESTAURANTS
-- =========================================================

CREATE TABLE restaurant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    photo_url TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================================================
-- 2. PERMISSIONS
-- =========================================================

CREATE TABLE permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

INSERT INTO permission (code, description) VALUES
    ('MENU_VIEW', 'Consulter le menu'),
    ('MENU_MANAGE', 'Gérer le menu'),
    ('DISH_AVAILABILITY_UPDATE', 'Modifier la disponibilité des plats'),
    ('TABLE_VIEW', 'Consulter les tables'),
    ('TABLE_MANAGE', 'Gérer les tables'),
    ('STAFF_MANAGE', 'Gérer le personnel'),
    ('ROLE_MANAGE', 'Gérer les rôles et leurs permissions'),
    ('PAYMENT_SETTINGS_MANAGE', 'Gérer les paramètres de paiement'),
    ('ORDER_CREATE', 'Créer, modifier un brouillon et envoyer une commande'),
    ('BILL_REQUEST', 'Demander une addition'),
    ('KITCHEN_VIEW', 'Consulter les commandes en cuisine'),
    ('ORDER_PREPARE', 'Gérer la préparation des commandes'),
    ('PAYMENT_COLLECT', 'Encaisser les paiements'),
    ('PLATFORM_RESTAURANT_VIEW', 'Consulter les restaurants de la plateforme'),
    ('PLATFORM_RESTAURANT_CREATE', 'Créer un restaurant'),
    ('PLATFORM_RESTAURANT_UPDATE', 'Modifier un restaurant'),
    ('PLATFORM_RESTAURANT_DISABLE', 'Désactiver un restaurant'),
    ('PLATFORM_RESTAURANT_REACTIVATE', 'Réactiver un restaurant');

-- =========================================================
-- 3. ROLES (propres à chaque restaurant)
-- =========================================================

CREATE TABLE role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_role_restaurant
        FOREIGN KEY (restaurant_id)
        REFERENCES restaurant(id)
        ON DELETE RESTRICT,

    -- Nécessaire pour la clé composite de users.
    CONSTRAINT uq_role_id_restaurant
        UNIQUE (id, restaurant_id)
);

-- Noms uniques dans chaque restaurant.
CREATE UNIQUE INDEX uq_role_restaurant_name
    ON role (restaurant_id, LOWER(name));

-- =========================================================
-- 4. ASSOCIATIONS ROLE / PERMISSION
-- =========================================================

CREATE TABLE role_permission (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,

    CONSTRAINT pk_role_permission
        PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id)
        REFERENCES role(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (permission_id)
        REFERENCES permission(id)
        ON DELETE CASCADE
);

-- =========================================================
-- 5. UTILISATEURS
-- =========================================================
-- Admin plateforme : is_platform_admin, sans restaurant ni rôle.
-- Employé : restaurant et rôle obligatoires.

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    photo_url TEXT,

    is_platform_admin BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,

    restaurant_id UUID,
    role_id UUID,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_users_failed_login_attempts
        CHECK (failed_login_attempts >= 0),

    CONSTRAINT chk_users_admin_assignment
        CHECK (
            (is_platform_admin = TRUE
                AND restaurant_id IS NULL
                AND role_id IS NULL)
            OR
            (is_platform_admin = FALSE
                AND restaurant_id IS NOT NULL
                AND role_id IS NOT NULL)
        ),

    CONSTRAINT fk_users_restaurant
        FOREIGN KEY (restaurant_id)
        REFERENCES restaurant(id)
        ON DELETE RESTRICT,

    -- Empêche d'affecter un rôle appartenant à un autre restaurant.
    CONSTRAINT fk_users_role_restaurant
        FOREIGN KEY (role_id, restaurant_id)
        REFERENCES role(id, restaurant_id)
        ON DELETE RESTRICT
);

-- Adresse unique sans distinction majuscules/minuscules.
CREATE UNIQUE INDEX uq_users_email_lower
    ON users (LOWER(email));

CREATE INDEX idx_users_restaurant_id
    ON users (restaurant_id);

CREATE INDEX idx_users_role_restaurant
    ON users (role_id, restaurant_id);

-- =========================================================
-- 6. PERMISSIONS DIRECTES DE L'ADMIN PLATEFORME
-- =========================================================
-- L'admin plateforme n'a pas de rôle : ses permissions PLATFORM_*
-- lui sont attribuées directement.

CREATE TABLE user_permission (
    user_id UUID NOT NULL,
    permission_id UUID NOT NULL,

    CONSTRAINT pk_user_permission
        PRIMARY KEY (user_id, permission_id),

    CONSTRAINT fk_user_permission_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_permission_permission
        FOREIGN KEY (permission_id)
        REFERENCES permission(id)
        ON DELETE CASCADE
);

-- =========================================================
-- 7. JETONS
-- =========================================================
-- Seul le hash est stocké, jamais le jeton en clair.
-- family_id regroupe une chaîne de refresh tokens
-- pour tout révoquer en cas de réutilisation.

CREATE TABLE token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL,
    family_id UUID,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_token_type
        CHECK (type IN ('REFRESH', 'ACTIVATION', 'PASSWORD_RESET')),

    -- Un REFRESH a toujours une famille, les autres types n'en ont pas.
    CONSTRAINT chk_token_family
        CHECK ((type = 'REFRESH') = (family_id IS NOT NULL))
);

CREATE INDEX idx_token_user_type
    ON token (user_id, type);

CREATE INDEX idx_token_family_id
    ON token (family_id);

-- =========================================================
-- 8. JOURNAL D'AUDIT
-- =========================================================

CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id UUID,
    details JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_audit_log_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);
