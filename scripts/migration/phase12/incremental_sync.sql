-- Phase12 incremental migration (v2)
-- Source DB: labor_system
-- Target DB: continew_admin

CREATE DATABASE IF NOT EXISTS `continew_admin`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `continew_admin`.`migration_phase12_checkpoint_v2` (
    `source_table` varchar(64) NOT NULL,
    `last_sync_at` datetime NOT NULL DEFAULT '1970-01-01 00:00:00',
    `last_sync_id` bigint NOT NULL DEFAULT 0,
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`source_table`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @cp_customer_time := COALESCE((
    SELECT `last_sync_at`
    FROM `continew_admin`.`migration_phase12_checkpoint_v2`
    WHERE `source_table` = 'customer'
), '1970-01-01 00:00:00');
SET @cp_customer_id := COALESCE((
    SELECT `last_sync_id`
    FROM `continew_admin`.`migration_phase12_checkpoint_v2`
    WHERE `source_table` = 'customer'
), 0);

SET @cp_contract_time := COALESCE((
    SELECT `last_sync_at`
    FROM `continew_admin`.`migration_phase12_checkpoint_v2`
    WHERE `source_table` = 'labor_contract'
), '1970-01-01 00:00:00');
SET @cp_contract_id := COALESCE((
    SELECT `last_sync_id`
    FROM `continew_admin`.`migration_phase12_checkpoint_v2`
    WHERE `source_table` = 'labor_contract'
), 0);

SET @cp_settlement_time := COALESCE((
    SELECT `last_sync_at`
    FROM `continew_admin`.`migration_phase12_checkpoint_v2`
    WHERE `source_table` = 'settlement_rule'
), '1970-01-01 00:00:00');
SET @cp_settlement_id := COALESCE((
    SELECT `last_sync_id`
    FROM `continew_admin`.`migration_phase12_checkpoint_v2`
    WHERE `source_table` = 'settlement_rule'
), 0);

SET @cp_audit_time := COALESCE((
    SELECT `last_sync_at`
    FROM `continew_admin`.`migration_phase12_checkpoint_v2`
    WHERE `source_table` = 'audit_log'
), '1970-01-01 00:00:00');
SET @cp_audit_id := COALESCE((
    SELECT `last_sync_id`
    FROM `continew_admin`.`migration_phase12_checkpoint_v2`
    WHERE `source_table` = 'audit_log'
), 0);

INSERT INTO `continew_admin`.`labor_lead`
(`id`, `lead_code`, `project_name`, `contact_name`, `contact_phone`, `industry_type`, `biz_owner_id`, `cooperation_status`, `tender_at`, `deposit_status`, `create_user`, `create_time`, `update_user`, `update_time`, `deleted`)
SELECT
    c.id,
    TRIM(c.customer_code),
    TRIM(c.customer_name),
    NULLIF(TRIM(c.contact_name), ''),
    NULLIF(TRIM(c.contact_mobile), ''),
    'GENERAL',
    IFNULL(NULLIF(c.updated_by, 0), IFNULL(c.created_by, 0)),
    CASE UPPER(TRIM(c.status))
        WHEN 'ACTIVE' THEN 'FOLLOWING'
        WHEN 'INACTIVE' THEN 'LOST'
        ELSE 'NEW'
    END,
    NULL,
    'UNKNOWN',
    IFNULL(c.created_by, 0),
    c.created_at,
    IFNULL(c.updated_by, 0),
    c.updated_at,
    CASE WHEN IFNULL(c.is_deleted, 0) = 0 THEN 0 ELSE c.id END
FROM `labor_system`.`customer` c
WHERE (c.updated_at > @cp_customer_time)
   OR (c.updated_at = @cp_customer_time AND c.id > @cp_customer_id)
ON DUPLICATE KEY UPDATE
    `lead_code` = VALUES(`lead_code`),
    `project_name` = VALUES(`project_name`),
    `contact_name` = VALUES(`contact_name`),
    `contact_phone` = VALUES(`contact_phone`),
    `industry_type` = VALUES(`industry_type`),
    `biz_owner_id` = VALUES(`biz_owner_id`),
    `cooperation_status` = VALUES(`cooperation_status`),
    `tender_at` = VALUES(`tender_at`),
    `deposit_status` = VALUES(`deposit_status`),
    `update_user` = VALUES(`update_user`),
    `update_time` = VALUES(`update_time`),
    `deleted` = VALUES(`deleted`);

INSERT INTO `continew_admin`.`labor_employer`
(`id`, `unit_code`, `lead_id`, `unit_name`, `customer_level`, `address`, `invoice_info`, `is_outsource`, `create_user`, `create_time`, `update_user`, `update_time`, `deleted`)
SELECT
    c.id,
    TRIM(c.customer_code),
    c.id,
    TRIM(c.customer_name),
    UPPER(COALESCE(NULLIF(TRIM(c.customer_level), ''), 'C')),
    NULLIF(TRIM(c.address), ''),
    JSON_OBJECT(),
    b'0',
    IFNULL(c.created_by, 0),
    c.created_at,
    IFNULL(c.updated_by, 0),
    c.updated_at,
    CASE WHEN IFNULL(c.is_deleted, 0) = 0 THEN 0 ELSE c.id END
FROM `labor_system`.`customer` c
WHERE (c.updated_at > @cp_customer_time)
   OR (c.updated_at = @cp_customer_time AND c.id > @cp_customer_id)
ON DUPLICATE KEY UPDATE
    `unit_code` = VALUES(`unit_code`),
    `lead_id` = VALUES(`lead_id`),
    `unit_name` = VALUES(`unit_name`),
    `customer_level` = VALUES(`customer_level`),
    `address` = VALUES(`address`),
    `invoice_info` = VALUES(`invoice_info`),
    `is_outsource` = VALUES(`is_outsource`),
    `update_user` = VALUES(`update_user`),
    `update_time` = VALUES(`update_time`),
    `deleted` = VALUES(`deleted`);

REPLACE INTO `continew_admin`.`migration_phase12_checkpoint_v2` (`source_table`, `last_sync_at`, `last_sync_id`, `updated_at`)
SELECT
    'customer',
    COALESCE(MAX(mx.max_time), @cp_customer_time),
    COALESCE(MAX(CASE WHEN c.updated_at = mx.max_time THEN c.id END), @cp_customer_id),
    NOW()
FROM (
    SELECT MAX(updated_at) AS max_time
    FROM `labor_system`.`customer`
    WHERE (updated_at > @cp_customer_time)
       OR (updated_at = @cp_customer_time AND id > @cp_customer_id)
) mx
LEFT JOIN `labor_system`.`customer` c
    ON c.updated_at = mx.max_time
   AND ((c.updated_at > @cp_customer_time)
     OR (c.updated_at = @cp_customer_time AND c.id > @cp_customer_id));

INSERT INTO `continew_admin`.`labor_contract`
(`id`, `contract_no`, `employer_unit_id`, `contract_name`, `contract_type`, `start_date`, `end_date`, `settlement_cycle`, `status`, `tax_rate`, `create_user`, `create_time`, `update_user`, `update_time`, `deleted`)
SELECT
    lc.id,
    COALESCE(NULLIF(TRIM(lc.contract_code), ''), CONCAT('LC-', lc.id)),
    lc.customer_id,
    TRIM(lc.contract_name),
    COALESCE(NULLIF(UPPER(TRIM(lc.contract_type)), ''), 'LABOR'),
    lc.start_date,
    lc.end_date,
    'MONTHLY',
    CASE UPPER(TRIM(lc.status))
        WHEN 'ACTIVE' THEN 'SIGNED'
        WHEN 'TERMINATED' THEN 'TERMINATED'
        WHEN 'CANCELLED' THEN 'TERMINATED'
        ELSE 'DRAFT'
    END,
    0,
    IFNULL(lc.created_by, 0),
    lc.created_at,
    IFNULL(lc.updated_by, 0),
    lc.updated_at,
    CASE WHEN IFNULL(lc.is_deleted, 0) = 0 THEN 0 ELSE lc.id END
FROM `labor_system`.`labor_contract` lc
WHERE (lc.updated_at > @cp_contract_time)
   OR (lc.updated_at = @cp_contract_time AND lc.id > @cp_contract_id)
ON DUPLICATE KEY UPDATE
    `contract_no` = VALUES(`contract_no`),
    `employer_unit_id` = VALUES(`employer_unit_id`),
    `contract_name` = VALUES(`contract_name`),
    `contract_type` = VALUES(`contract_type`),
    `start_date` = VALUES(`start_date`),
    `end_date` = VALUES(`end_date`),
    `settlement_cycle` = VALUES(`settlement_cycle`),
    `status` = VALUES(`status`),
    `tax_rate` = VALUES(`tax_rate`),
    `update_user` = VALUES(`update_user`),
    `update_time` = VALUES(`update_time`),
    `deleted` = VALUES(`deleted`);

REPLACE INTO `continew_admin`.`migration_phase12_checkpoint_v2` (`source_table`, `last_sync_at`, `last_sync_id`, `updated_at`)
SELECT
    'labor_contract',
    COALESCE(MAX(mx.max_time), @cp_contract_time),
    COALESCE(MAX(CASE WHEN lc.updated_at = mx.max_time THEN lc.id END), @cp_contract_id),
    NOW()
FROM (
    SELECT MAX(updated_at) AS max_time
    FROM `labor_system`.`labor_contract`
    WHERE (updated_at > @cp_contract_time)
       OR (updated_at = @cp_contract_time AND id > @cp_contract_id)
) mx
LEFT JOIN `labor_system`.`labor_contract` lc
    ON lc.updated_at = mx.max_time
   AND ((lc.updated_at > @cp_contract_time)
     OR (lc.updated_at = @cp_contract_time AND lc.id > @cp_contract_id));

INSERT INTO `continew_admin`.`labor_settlement_rule`
(`contract_id`, `rule_type`, `version_no`, `effective_from`, `rule_payload`, `status`, `published_at`, `deactivated_at`, `create_user`, `create_time`, `update_user`, `update_time`, `deleted`)
SELECT
    sc.contract_id,
    sc.rule_type,
    sc.version_no,
    sc.effective_from,
    sc.rule_payload,
    sc.status,
    sc.published_at,
    sc.deactivated_at,
    sc.create_user,
    sc.create_time,
    sc.update_user,
    sc.update_time,
    sc.deleted
FROM (
    SELECT
        lc.id AS contract_id,
        COALESCE(NULLIF(UPPER(TRIM(sr.rule_type)), ''), 'GENERAL') AS rule_type,
        ROW_NUMBER() OVER (PARTITION BY lc.id ORDER BY sr.updated_at, sr.id) AS version_no,
        COALESCE(sr.effective_from, lc.start_date, CURRENT_DATE()) AS effective_from,
        JSON_OBJECT(
            'ruleName', sr.rule_name,
            'formulaDsl', sr.formula_dsl,
            'effectiveTo', IF(sr.effective_to IS NULL, NULL, DATE_FORMAT(sr.effective_to, '%Y-%m-%d'))
        ) AS rule_payload,
        CASE UPPER(TRIM(sr.status))
            WHEN 'ACTIVE' THEN 'PUBLISHED'
            ELSE 'DRAFT'
        END AS status,
        CASE WHEN UPPER(TRIM(sr.status)) = 'ACTIVE' THEN sr.updated_at ELSE NULL END AS published_at,
        CASE WHEN UPPER(TRIM(sr.status)) <> 'ACTIVE' THEN sr.updated_at ELSE NULL END AS deactivated_at,
        IFNULL(sr.created_by, 0) AS create_user,
        sr.created_at AS create_time,
        IFNULL(sr.updated_by, 0) AS update_user,
        GREATEST(sr.updated_at, lc.updated_at) AS update_time,
        CASE
            WHEN IFNULL(sr.is_deleted, 0) = 0 AND IFNULL(lc.is_deleted, 0) = 0 THEN 0
            ELSE lc.id
        END AS deleted
    FROM `labor_system`.`labor_contract` lc
    INNER JOIN `labor_system`.`settlement_rule` sr
        ON lc.settlement_rule_id = sr.id
    WHERE (sr.updated_at > @cp_settlement_time)
       OR (sr.updated_at = @cp_settlement_time AND sr.id > @cp_settlement_id)
       OR (lc.updated_at > @cp_contract_time)
       OR (lc.updated_at = @cp_contract_time AND lc.id > @cp_contract_id)
) sc
ON DUPLICATE KEY UPDATE
    `rule_type` = VALUES(`rule_type`),
    `effective_from` = VALUES(`effective_from`),
    `rule_payload` = VALUES(`rule_payload`),
    `status` = VALUES(`status`),
    `published_at` = VALUES(`published_at`),
    `deactivated_at` = VALUES(`deactivated_at`),
    `update_user` = VALUES(`update_user`),
    `update_time` = VALUES(`update_time`),
    `deleted` = VALUES(`deleted`);

REPLACE INTO `continew_admin`.`migration_phase12_checkpoint_v2` (`source_table`, `last_sync_at`, `last_sync_id`, `updated_at`)
SELECT
    'settlement_rule',
    COALESCE(MAX(mx.max_time), @cp_settlement_time),
    COALESCE(MAX(CASE WHEN sr.updated_at = mx.max_time THEN sr.id END), @cp_settlement_id),
    NOW()
FROM (
    SELECT MAX(updated_at) AS max_time
    FROM `labor_system`.`settlement_rule`
    WHERE (updated_at > @cp_settlement_time)
       OR (updated_at = @cp_settlement_time AND id > @cp_settlement_id)
) mx
LEFT JOIN `labor_system`.`settlement_rule` sr
    ON sr.updated_at = mx.max_time
   AND ((sr.updated_at > @cp_settlement_time)
     OR (sr.updated_at = @cp_settlement_time AND sr.id > @cp_settlement_id));

INSERT INTO `continew_admin`.`labor_lead_follow_up`
(`id`, `lead_id`, `action`, `content`, `status`, `status_from`, `status_to`, `next_contact_at`, `operator_id`, `create_user`, `create_time`, `update_user`, `update_time`, `deleted`)
SELECT
    al.id,
    CAST(al.biz_id AS UNSIGNED),
    UPPER(TRIM(al.action)),
    CASE WHEN JSON_VALID(al.detail) THEN JSON_UNQUOTE(JSON_EXTRACT(al.detail, '$.content')) ELSE NULL END,
    CASE WHEN JSON_VALID(al.detail) THEN JSON_UNQUOTE(JSON_EXTRACT(al.detail, '$.status')) ELSE NULL END,
    CASE WHEN JSON_VALID(al.detail) THEN JSON_UNQUOTE(JSON_EXTRACT(al.detail, '$.statusFrom')) ELSE NULL END,
    CASE WHEN JSON_VALID(al.detail) THEN JSON_UNQUOTE(JSON_EXTRACT(al.detail, '$.statusTo')) ELSE NULL END,
    CASE
        WHEN JSON_VALID(al.detail)
             AND JSON_UNQUOTE(JSON_EXTRACT(al.detail, '$.nextContactAt')) IS NOT NULL
            THEN CAST(REPLACE(JSON_UNQUOTE(JSON_EXTRACT(al.detail, '$.nextContactAt')), 'T', ' ') AS DATETIME)
        ELSE NULL
    END,
    IFNULL(al.operator_id, 0),
    IFNULL(al.operator_id, 0),
    al.created_at,
    IFNULL(al.operator_id, 0),
    al.created_at,
    0
FROM `labor_system`.`audit_log` al
WHERE al.biz_type = 'LEAD_FOLLOW_UP'
  AND al.biz_id REGEXP '^[0-9]+$'
  AND ((al.created_at > @cp_audit_time)
    OR (al.created_at = @cp_audit_time AND al.id > @cp_audit_id))
ON DUPLICATE KEY UPDATE
    `lead_id` = VALUES(`lead_id`),
    `action` = VALUES(`action`),
    `content` = VALUES(`content`),
    `status` = VALUES(`status`),
    `status_from` = VALUES(`status_from`),
    `status_to` = VALUES(`status_to`),
    `next_contact_at` = VALUES(`next_contact_at`),
    `operator_id` = VALUES(`operator_id`),
    `update_user` = VALUES(`update_user`),
    `update_time` = VALUES(`update_time`),
    `deleted` = VALUES(`deleted`);

REPLACE INTO `continew_admin`.`migration_phase12_checkpoint_v2` (`source_table`, `last_sync_at`, `last_sync_id`, `updated_at`)
SELECT
    'audit_log',
    COALESCE(MAX(mx.max_time), @cp_audit_time),
    COALESCE(MAX(CASE WHEN al.created_at = mx.max_time THEN al.id END), @cp_audit_id),
    NOW()
FROM (
    SELECT MAX(created_at) AS max_time
    FROM `labor_system`.`audit_log`
    WHERE biz_type = 'LEAD_FOLLOW_UP'
      AND ((created_at > @cp_audit_time)
        OR (created_at = @cp_audit_time AND id > @cp_audit_id))
) mx
LEFT JOIN `labor_system`.`audit_log` al
    ON al.biz_type = 'LEAD_FOLLOW_UP'
   AND al.created_at = mx.max_time
   AND ((al.created_at > @cp_audit_time)
     OR (al.created_at = @cp_audit_time AND al.id > @cp_audit_id));
