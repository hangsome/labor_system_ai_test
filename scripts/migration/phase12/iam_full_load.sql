-- Phase01 IAM full load migration (v2)
-- Source DB: labor_system
-- Target DB: continew_admin

CREATE DATABASE IF NOT EXISTS `continew_admin`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE `continew_admin`;

CREATE TABLE IF NOT EXISTS `continew_admin`.`migration_phase12_iam_checkpoint_v2` (
    `source_table` varchar(64) NOT NULL,
    `last_sync_at` datetime NOT NULL DEFAULT '1970-01-01 00:00:00',
    `last_sync_id` bigint NOT NULL DEFAULT 0,
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`source_table`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 1) Role migration (except reserved ADMIN/MANAGER fallback roles)
INSERT INTO `continew_admin`.`sys_role`
(`name`, `code`, `data_scope`, `description`, `sort`, `is_system`, `menu_check_strictly`, `dept_check_strictly`, `create_user`, `create_time`, `update_user`, `update_time`, `deleted`, `tenant_id`)
SELECT
    COALESCE(NULLIF(TRIM(r.role_name), ''), CONCAT('Role-', r.id)),
    LOWER(TRIM(r.role_code)),
    4,
    CONCAT('Migrated from labor_system.role#', r.id),
    900,
    b'0',
    b'1',
    b'1',
    NULLIF(r.created_by, 0),
    r.created_at,
    NULLIF(r.updated_by, 0),
    r.updated_at,
    CASE WHEN IFNULL(r.is_deleted, 0) = 0 THEN 0 ELSE r.id END,
    0
FROM `labor_system`.`role` r
WHERE UPPER(TRIM(r.role_code)) NOT IN ('ADMIN', 'MANAGER')
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `update_user` = VALUES(`update_user`),
    `update_time` = VALUES(`update_time`),
    `deleted` = VALUES(`deleted`);

DROP TEMPORARY TABLE IF EXISTS `tmp_phase12_role_map`;
CREATE TEMPORARY TABLE `tmp_phase12_role_map` (
    `source_role_id` bigint NOT NULL,
    `target_role_id` bigint NOT NULL,
    PRIMARY KEY (`source_role_id`)
) ENGINE=Memory;

-- Fallback mapping when role_permission is empty: ADMIN->1(super_admin), MANAGER->2(sys_admin)
INSERT INTO `tmp_phase12_role_map` (`source_role_id`, `target_role_id`)
SELECT r.id,
       CASE UPPER(TRIM(r.role_code))
           WHEN 'ADMIN' THEN 1
           WHEN 'MANAGER' THEN 2
       END AS target_role_id
FROM `labor_system`.`role` r
WHERE IFNULL(r.is_deleted, 0) = 0
  AND UPPER(TRIM(r.role_code)) IN ('ADMIN', 'MANAGER');

-- Map all other source roles to generated sys_role rows
INSERT INTO `tmp_phase12_role_map` (`source_role_id`, `target_role_id`)
SELECT r.id, sr.id
FROM `labor_system`.`role` r
INNER JOIN `continew_admin`.`sys_role` sr
    ON CONVERT(sr.code USING utf8mb4) COLLATE utf8mb4_unicode_ci
       = CONVERT(LOWER(TRIM(r.role_code)) USING utf8mb4) COLLATE utf8mb4_unicode_ci
   AND sr.deleted = CASE WHEN IFNULL(r.is_deleted, 0) = 0 THEN 0 ELSE r.id END
   AND sr.tenant_id = 0
WHERE IFNULL(r.is_deleted, 0) = 0
  AND UPPER(TRIM(r.role_code)) NOT IN ('ADMIN', 'MANAGER')
ON DUPLICATE KEY UPDATE
    `target_role_id` = VALUES(`target_role_id`);

-- 2) User migration
INSERT INTO `continew_admin`.`sys_user`
(`id`, `username`, `nickname`, `password`, `gender`, `email`, `phone`, `avatar`, `description`, `status`, `is_system`, `pwd_reset_time`, `dept_id`, `create_user`, `create_time`, `update_user`, `update_time`, `deleted`, `tenant_id`)
SELECT
    ua.id,
    TRIM(ua.username),
    COALESCE(NULLIF(TRIM(ua.display_name), ''), TRIM(ua.username)),
    CASE
        WHEN ua.password_hash IS NULL OR TRIM(ua.password_hash) = '' THEN NULL
        WHEN TRIM(ua.password_hash) REGEXP '^\\{.+\\}.*' THEN TRIM(ua.password_hash)
        ELSE CONCAT('{bcrypt}', TRIM(ua.password_hash))
    END,
    0,
    NULL,
    NULLIF(TRIM(ua.mobile), ''),
    NULL,
    CONCAT('Migrated from labor_system.user_account#', ua.id),
    CASE UPPER(TRIM(ua.status))
        WHEN 'INACTIVE' THEN 2
        ELSE 1
    END,
    CASE WHEN UPPER(TRIM(ua.username)) = 'ADMIN' THEN b'1' ELSE b'0' END,
    NULL,
    1,
    NULLIF(ua.created_by, 0),
    ua.created_at,
    NULLIF(ua.updated_by, 0),
    ua.updated_at,
    CASE WHEN IFNULL(ua.is_deleted, 0) = 0 THEN 0 ELSE ua.id END,
    0
FROM `labor_system`.`user_account` ua
ON DUPLICATE KEY UPDATE
    `nickname` = VALUES(`nickname`),
    `password` = VALUES(`password`),
    `phone` = VALUES(`phone`),
    `status` = VALUES(`status`),
    `dept_id` = VALUES(`dept_id`),
    `update_user` = VALUES(`update_user`),
    `update_time` = VALUES(`update_time`),
    `deleted` = VALUES(`deleted`);

DROP TEMPORARY TABLE IF EXISTS `tmp_phase12_user_map`;
CREATE TEMPORARY TABLE `tmp_phase12_user_map` (
    `source_user_id` bigint NOT NULL,
    `target_user_id` bigint NOT NULL,
    PRIMARY KEY (`source_user_id`)
) ENGINE=Memory;

INSERT INTO `tmp_phase12_user_map` (`source_user_id`, `target_user_id`)
SELECT
    ua.id,
    su.id
FROM `labor_system`.`user_account` ua
INNER JOIN `continew_admin`.`sys_user` su
    ON CONVERT(su.username USING utf8mb4) COLLATE utf8mb4_unicode_ci
       = CONVERT(TRIM(ua.username) USING utf8mb4) COLLATE utf8mb4_unicode_ci
   AND su.deleted = CASE WHEN IFNULL(ua.is_deleted, 0) = 0 THEN 0 ELSE ua.id END
   AND su.tenant_id = 0;

-- 3) User-role relation (full refresh for migrated users)
DELETE sur
FROM `continew_admin`.`sys_user_role` sur
INNER JOIN `tmp_phase12_user_map` um
    ON sur.user_id = um.target_user_id;

INSERT INTO `continew_admin`.`sys_user_role`
(`user_id`, `role_id`, `tenant_id`)
SELECT
    um.target_user_id,
    rm.target_role_id,
    0
FROM `labor_system`.`user_role` ur
INNER JOIN `tmp_phase12_user_map` um
    ON um.source_user_id = ur.user_id
INNER JOIN `tmp_phase12_role_map` rm
    ON rm.source_role_id = ur.role_id
WHERE IFNULL(ur.is_deleted, 0) = 0
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`);

-- 4) Role-menu relation
SET @rp_count := (SELECT COUNT(*) FROM `labor_system`.`role_permission`);

-- Sync non-system mapped roles from permission code -> sys_menu.permission
DELETE srm
FROM `continew_admin`.`sys_role_menu` srm
INNER JOIN `tmp_phase12_role_map` rm
    ON srm.role_id = rm.target_role_id
WHERE rm.target_role_id NOT IN (1, 2)
  AND @rp_count > 0;

INSERT INTO `continew_admin`.`sys_role_menu`
(`role_id`, `menu_id`, `tenant_id`)
SELECT DISTINCT
    rm.target_role_id,
    sm.id,
    0
FROM `labor_system`.`role_permission` rp
INNER JOIN `tmp_phase12_role_map` rm
    ON rm.source_role_id = rp.role_id
INNER JOIN `labor_system`.`permission` p
    ON p.id = rp.permission_id
INNER JOIN `continew_admin`.`sys_menu` sm
    ON CONVERT(sm.permission USING utf8mb4) COLLATE utf8mb4_unicode_ci
       = CONVERT(p.code USING utf8mb4) COLLATE utf8mb4_unicode_ci
   AND sm.deleted = 0
WHERE @rp_count > 0
  AND p.code IS NOT NULL
  AND TRIM(p.code) <> ''
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`);

-- 5) IAM checkpoints
REPLACE INTO `continew_admin`.`migration_phase12_iam_checkpoint_v2` (`source_table`, `last_sync_at`, `last_sync_id`, `updated_at`)
SELECT
    'user_account',
    COALESCE(MAX(mx.max_time), '1970-01-01 00:00:00'),
    COALESCE(MAX(ua.id), 0),
    NOW()
FROM (SELECT MAX(updated_at) AS max_time FROM `labor_system`.`user_account`) mx
LEFT JOIN `labor_system`.`user_account` ua ON ua.updated_at = mx.max_time;

REPLACE INTO `continew_admin`.`migration_phase12_iam_checkpoint_v2` (`source_table`, `last_sync_at`, `last_sync_id`, `updated_at`)
SELECT
    'role',
    COALESCE(MAX(mx.max_time), '1970-01-01 00:00:00'),
    COALESCE(MAX(r.id), 0),
    NOW()
FROM (SELECT MAX(updated_at) AS max_time FROM `labor_system`.`role`) mx
LEFT JOIN `labor_system`.`role` r ON r.updated_at = mx.max_time;

REPLACE INTO `continew_admin`.`migration_phase12_iam_checkpoint_v2` (`source_table`, `last_sync_at`, `last_sync_id`, `updated_at`)
SELECT
    'user_role',
    COALESCE(MAX(mx.max_time), '1970-01-01 00:00:00'),
    COALESCE(MAX(ur.id), 0),
    NOW()
FROM (SELECT MAX(updated_at) AS max_time FROM `labor_system`.`user_role`) mx
LEFT JOIN `labor_system`.`user_role` ur ON ur.updated_at = mx.max_time;

SET SESSION group_concat_max_len = 1024 * 1024;
SET @permission_checksum := COALESCE((
    SELECT CRC32(GROUP_CONCAT(CONCAT(id, ':', IFNULL(code, '')) ORDER BY id SEPARATOR '|'))
    FROM `labor_system`.`permission`
), 0);
SET @role_permission_checksum := COALESCE((
    SELECT CRC32(GROUP_CONCAT(CONCAT(role_id, ':', permission_id) ORDER BY role_id, permission_id SEPARATOR '|'))
    FROM `labor_system`.`role_permission`
), 0);

REPLACE INTO `continew_admin`.`migration_phase12_iam_checkpoint_v2`
(`source_table`, `last_sync_at`, `last_sync_id`, `updated_at`)
VALUES
('permission', NOW(), @permission_checksum, NOW()),
('role_permission', NOW(), @role_permission_checksum, NOW());
