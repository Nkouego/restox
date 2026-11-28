
-- V3__add_restaurant_settings_permission.sql

-- =========================================================
-- 1. NOUVELLE PERMISSION
-- =========================================================

INSERT INTO permission (code, description)
VALUES (
    'RESTAURANT_SETTINGS_MANAGE',
    'Modifier les paramètres du restaurant'
);

-- =========================================================
-- 2. ATTRIBUTION AUX MANAGERS EXISTANTS
-- =========================================================

INSERT INTO role_permission (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM role r
CROSS JOIN permission p
WHERE LOWER(r.name) = 'manager'
  AND p.code = 'RESTAURANT_SETTINGS_MANAGE';
