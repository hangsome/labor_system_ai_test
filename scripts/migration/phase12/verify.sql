-- Phase12 migration verify script (v2)
-- Source DB: labor_system
-- Target DB: continew_admin

SELECT 'customer -> labor_lead (active)' AS check_item,
       (SELECT COUNT(*) FROM `labor_system`.`customer` WHERE IFNULL(is_deleted, 0) = 0) AS source_count,
       (SELECT COUNT(*) FROM `continew_admin`.`labor_lead` WHERE deleted = 0) AS target_count;

SELECT 'customer -> labor_employer (active)' AS check_item,
       (SELECT COUNT(*) FROM `labor_system`.`customer` WHERE IFNULL(is_deleted, 0) = 0) AS source_count,
       (SELECT COUNT(*) FROM `continew_admin`.`labor_employer` WHERE deleted = 0) AS target_count;

SELECT 'labor_contract -> labor_contract (active)' AS check_item,
       (SELECT COUNT(*) FROM `labor_system`.`labor_contract` WHERE IFNULL(is_deleted, 0) = 0) AS source_count,
       (SELECT COUNT(*) FROM `continew_admin`.`labor_contract` WHERE deleted = 0) AS target_count;

SELECT 'contract_with_rule -> labor_settlement_rule (active)' AS check_item,
       (
         SELECT COUNT(*)
         FROM `labor_system`.`labor_contract` lc
         INNER JOIN `labor_system`.`settlement_rule` sr ON sr.id = lc.settlement_rule_id
         WHERE IFNULL(lc.is_deleted, 0) = 0
           AND IFNULL(sr.is_deleted, 0) = 0
       ) AS source_count,
       (SELECT COUNT(*) FROM `continew_admin`.`labor_settlement_rule` WHERE deleted = 0) AS target_count;

SELECT 'audit_log(LEAD_FOLLOW_UP) -> labor_lead_follow_up' AS check_item,
       (SELECT COUNT(*) FROM `labor_system`.`audit_log` WHERE biz_type = 'LEAD_FOLLOW_UP') AS source_count,
       (SELECT COUNT(*) FROM `continew_admin`.`labor_lead_follow_up` WHERE deleted = 0) AS target_count;

SELECT 'missing_lead_ids' AS check_item, COUNT(*) AS mismatch_count
FROM `labor_system`.`customer` s
LEFT JOIN `continew_admin`.`labor_lead` t ON t.id = s.id
WHERE IFNULL(s.is_deleted, 0) = 0
  AND t.deleted <> 0;

SELECT 'missing_employer_ids' AS check_item, COUNT(*) AS mismatch_count
FROM `labor_system`.`customer` s
LEFT JOIN `continew_admin`.`labor_employer` t ON t.id = s.id
WHERE IFNULL(s.is_deleted, 0) = 0
  AND t.deleted <> 0;

SELECT 'missing_contract_ids' AS check_item, COUNT(*) AS mismatch_count
FROM `labor_system`.`labor_contract` s
LEFT JOIN `continew_admin`.`labor_contract` t ON t.id = s.id
WHERE IFNULL(s.is_deleted, 0) = 0
  AND t.deleted <> 0;

SELECT 'orphan_employer_lead' AS check_item, COUNT(*) AS mismatch_count
FROM `continew_admin`.`labor_employer` e
LEFT JOIN `continew_admin`.`labor_lead` l ON l.id = e.lead_id AND l.deleted = 0
WHERE e.deleted = 0
  AND e.lead_id IS NOT NULL
  AND l.id IS NULL;

SELECT 'orphan_contract_employer' AS check_item, COUNT(*) AS mismatch_count
FROM `continew_admin`.`labor_contract` c
LEFT JOIN `continew_admin`.`labor_employer` e ON e.id = c.employer_unit_id AND e.deleted = 0
WHERE c.deleted = 0
  AND e.id IS NULL;

SELECT 'orphan_rule_contract' AS check_item, COUNT(*) AS mismatch_count
FROM `continew_admin`.`labor_settlement_rule` r
LEFT JOIN `continew_admin`.`labor_contract` c ON c.id = r.contract_id AND c.deleted = 0
WHERE r.deleted = 0
  AND c.id IS NULL;

SELECT 'same_second_customer_rows' AS check_item,
       (
         SELECT COUNT(*)
         FROM `labor_system`.`customer` src
         WHERE src.updated_at = (SELECT MAX(updated_at) FROM `labor_system`.`customer`)
       ) AS source_count,
       (
         SELECT COUNT(*)
         FROM `continew_admin`.`labor_lead` tgt
         WHERE tgt.id IN (
            SELECT src.id
            FROM `labor_system`.`customer` src
            WHERE src.updated_at = (SELECT MAX(updated_at) FROM `labor_system`.`customer`)
         )
       ) AS target_count;

SELECT 'same_second_audit_rows' AS check_item,
       (
         SELECT COUNT(*)
         FROM `labor_system`.`audit_log` src
         WHERE src.biz_type = 'LEAD_FOLLOW_UP'
           AND src.created_at = (
             SELECT MAX(created_at)
             FROM `labor_system`.`audit_log`
             WHERE biz_type = 'LEAD_FOLLOW_UP'
           )
       ) AS source_count,
       (
         SELECT COUNT(*)
         FROM `continew_admin`.`labor_lead_follow_up` tgt
         WHERE tgt.id IN (
             SELECT src.id
             FROM `labor_system`.`audit_log` src
             WHERE src.biz_type = 'LEAD_FOLLOW_UP'
               AND src.created_at = (
                   SELECT MAX(created_at)
                   FROM `labor_system`.`audit_log`
                   WHERE biz_type = 'LEAD_FOLLOW_UP'
               )
         )
       ) AS target_count;

SELECT 'checkpoint_v2' AS check_item, source_table, last_sync_at, last_sync_id, updated_at
FROM `continew_admin`.`migration_phase12_checkpoint_v2`
ORDER BY source_table;
