-- Labor demo seed data (for local continew_admin)
-- Purpose:
-- 1) Ensure labor menu titles are Chinese.
-- 2) Seed labor demo data aligned with module workflow:
--    Lead -> Employer -> Contract -> Settlement Rule.

SET NAMES utf8mb4;
START TRANSACTION;

-- Menu localization is handled by:
-- backend/continew-server/src/main/resources/db/changelog/mysql/plugin/labor_patch.sql

-- =========================
-- Lead
-- =========================
INSERT INTO `labor_lead`
(`lead_code`, `project_name`, `contact_name`, `contact_phone`, `industry_type`,
 `biz_owner_id`, `cooperation_status`, `tender_at`, `deposit_status`,
 `create_user`, `create_time`, `update_user`, `update_time`, `deleted`, `tenant_id`)
VALUES
('LD-2026-001', '上海虹桥酒店群劳务合作', '王婷', '13800001111', '酒店', 1, 'NEW',       '2026-03-05', 'UNPAID', 1, NOW(), 1, NOW(), 0, 0),
('LD-2026-002', '苏州工业园区制造外包', '陈刚', '13800002222', '制造', 1, 'FOLLOWING', '2026-02-20', 'PAID',   1, NOW(), 1, NOW(), 0, 0),
('LD-2026-003', '杭州仓配中心驻场服务', '李媛', '13800003333', '物流', 1, 'WON',       '2026-01-15', 'PAID',   1, NOW(), 1, NOW(), 0, 0),
('LD-2026-004', '宁波连锁门店弹性用工', '周凯', '13800004444', '零售', 1, 'LOST',      '2025-12-18', 'REFUND', 1, NOW(), 1, NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
`project_name`       = VALUES(`project_name`),
`contact_name`       = VALUES(`contact_name`),
`contact_phone`      = VALUES(`contact_phone`),
`industry_type`      = VALUES(`industry_type`),
`biz_owner_id`       = VALUES(`biz_owner_id`),
`cooperation_status` = VALUES(`cooperation_status`),
`tender_at`          = VALUES(`tender_at`),
`deposit_status`     = VALUES(`deposit_status`),
`update_user`        = 1,
`update_time`        = NOW(),
`deleted`            = 0,
`tenant_id`          = 0;

SET @lead_new  = (SELECT id FROM labor_lead WHERE lead_code = 'LD-2026-001' AND deleted = 0 AND tenant_id = 0 LIMIT 1);
SET @lead_fol  = (SELECT id FROM labor_lead WHERE lead_code = 'LD-2026-002' AND deleted = 0 AND tenant_id = 0 LIMIT 1);
SET @lead_won  = (SELECT id FROM labor_lead WHERE lead_code = 'LD-2026-003' AND deleted = 0 AND tenant_id = 0 LIMIT 1);
SET @lead_lost = (SELECT id FROM labor_lead WHERE lead_code = 'LD-2026-004' AND deleted = 0 AND tenant_id = 0 LIMIT 1);

DELETE FROM `labor_lead_follow_up`
WHERE `lead_id` IN (@lead_new, @lead_fol, @lead_won, @lead_lost) AND `tenant_id` = 0;

INSERT INTO `labor_lead_follow_up`
(`lead_id`, `action`, `content`, `status`, `status_from`, `status_to`, `next_contact_at`, `operator_id`,
 `create_user`, `create_time`, `update_user`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@lead_new,  'FOLLOW_UP',         '首次接触，等待客户回传需求清单', 'NEW',       NULL,        NULL,        DATE_ADD(NOW(), INTERVAL 3 DAY), 1, 1, NOW(), 1, NOW(), 0, 0),
(@lead_fol,  'STATUS_TRANSITION', '由 NEW 转入 FOLLOWING，已进入商务洽谈', 'FOLLOWING', 'NEW',      'FOLLOWING', NULL,                          1, 1, NOW(), 1, NOW(), 0, 0),
(@lead_fol,  'FOLLOW_UP',         '客户要求补充驻场人数与排班方案', 'FOLLOWING', NULL,        NULL,        DATE_ADD(NOW(), INTERVAL 2 DAY), 1, 1, NOW(), 1, NOW(), 0, 0),
(@lead_won,  'STATUS_TRANSITION', '商机成交，已确认合同条款',       'WON',       'FOLLOWING', 'WON',       NULL,                          1, 1, NOW(), 1, NOW(), 0, 0),
(@lead_lost, 'STATUS_TRANSITION', '预算未通过，本轮商机流失',       'LOST',      'FOLLOWING', 'LOST',      NULL,                          1, 1, NOW(), 1, NOW(), 0, 0);

-- =========================
-- Employer
-- =========================
INSERT INTO `labor_employer`
(`unit_code`, `lead_id`, `unit_name`, `customer_level`, `address`, `invoice_info`, `is_outsource`,
 `create_user`, `create_time`, `update_user`, `update_time`, `deleted`, `tenant_id`)
VALUES
('EMP-2026-001', @lead_won,  '杭州仓配运营有限公司', 'A',        '浙江省杭州市余杭区仓兴路 9 号',  JSON_OBJECT('taxNo','91330100MA000001','bank','中国银行杭州支行'), b'1', 1, NOW(), 1, NOW(), 0, 0),
('EMP-2026-002', @lead_fol,  '苏州精工制造服务有限公司', 'B',      '江苏省苏州市工业园区金鸡湖大道 88 号', JSON_OBJECT('taxNo','91320500MA000002','bank','建设银行苏州分行'), b'0', 1, NOW(), 1, NOW(), 0, 0),
('EMP-2026-003', @lead_new,  '上海虹桥酒店管理有限公司', 'C',      '上海市闵行区虹桥商务区申贵路 66 号', JSON_OBJECT('taxNo','91310100MA000003','bank','工商银行上海分行'), b'1', 1, NOW(), 1, NOW(), 0, 0),
('EMP-2026-004', @lead_lost, '宁波零售连锁管理有限公司', 'INACTIVE', '浙江省宁波市鄞州区中山东路 188 号', JSON_OBJECT('taxNo','91330200MA000004','bank','农业银行宁波分行'), b'0', 1, NOW(), 1, NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
`lead_id`         = VALUES(`lead_id`),
`unit_name`       = VALUES(`unit_name`),
`customer_level`  = VALUES(`customer_level`),
`address`         = VALUES(`address`),
`invoice_info`    = VALUES(`invoice_info`),
`is_outsource`    = VALUES(`is_outsource`),
`update_user`     = 1,
`update_time`     = NOW(),
`deleted`         = 0,
`tenant_id`       = 0;

SET @emp_a  = (SELECT id FROM labor_employer WHERE unit_code = 'EMP-2026-001' AND deleted = 0 AND tenant_id = 0 LIMIT 1);
SET @emp_b  = (SELECT id FROM labor_employer WHERE unit_code = 'EMP-2026-002' AND deleted = 0 AND tenant_id = 0 LIMIT 1);
SET @emp_c  = (SELECT id FROM labor_employer WHERE unit_code = 'EMP-2026-003' AND deleted = 0 AND tenant_id = 0 LIMIT 1);
SET @emp_in = (SELECT id FROM labor_employer WHERE unit_code = 'EMP-2026-004' AND deleted = 0 AND tenant_id = 0 LIMIT 1);

-- =========================
-- Contract
-- =========================
INSERT INTO `labor_contract`
(`contract_no`, `employer_unit_id`, `contract_name`, `contract_type`, `start_date`, `end_date`,
 `settlement_cycle`, `status`, `tax_rate`,
 `create_user`, `create_time`, `update_user`, `update_time`, `deleted`, `tenant_id`)
VALUES
('CT-2026-001', @emp_a,  '杭州仓配 2026 年度驻场合同', 'A', '2026-01-01', '2026-12-31', 'MONTHLY', 'SIGNED',     0.0600, 1, NOW(), 1, NOW(), 0, 0),
('CT-2026-002', @emp_b,  '苏州制造柔性用工框架合同',   'B', '2026-03-01', '2027-02-28', 'MONTHLY', 'DRAFT',      0.0300, 1, NOW(), 1, NOW(), 0, 0),
('CT-2026-003', @emp_c,  '上海酒店高峰期保障合同',     'A', '2025-07-01', '2026-06-30', 'WEEKLY',  'SIGNED',     0.0150, 1, NOW(), 1, NOW(), 0, 0),
('CT-2026-004', @emp_in, '宁波零售临促项目合同',       'B', '2025-01-01', '2025-10-31', 'MONTHLY', 'TERMINATED', 0.0500, 1, NOW(), 1, NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
`employer_unit_id` = VALUES(`employer_unit_id`),
`contract_name`    = VALUES(`contract_name`),
`contract_type`    = VALUES(`contract_type`),
`start_date`       = VALUES(`start_date`),
`end_date`         = VALUES(`end_date`),
`settlement_cycle` = VALUES(`settlement_cycle`),
`status`           = VALUES(`status`),
`tax_rate`         = VALUES(`tax_rate`),
`update_user`      = 1,
`update_time`      = NOW(),
`deleted`          = 0,
`tenant_id`        = 0;

SET @ct_1 = (SELECT id FROM labor_contract WHERE contract_no = 'CT-2026-001' AND deleted = 0 AND tenant_id = 0 LIMIT 1);
SET @ct_2 = (SELECT id FROM labor_contract WHERE contract_no = 'CT-2026-002' AND deleted = 0 AND tenant_id = 0 LIMIT 1);
SET @ct_3 = (SELECT id FROM labor_contract WHERE contract_no = 'CT-2026-003' AND deleted = 0 AND tenant_id = 0 LIMIT 1);

-- =========================
-- Settlement rules (with version/status coverage)
-- =========================
INSERT INTO `labor_settlement_rule`
(`contract_id`, `rule_type`, `version_no`, `effective_from`, `rule_payload`, `status`,
 `published_at`, `deactivated_at`,
 `create_user`, `create_time`, `update_user`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@ct_1, 'RATE', 1, '2026-01-01', JSON_OBJECT('mode','rate','base','salary','value',0.08), 'PUBLISHED', '2026-01-02 09:00:00', NULL,                    1, NOW(), 1, NOW(), 0, 0),
(@ct_1, 'RATE', 2, '2026-07-01', JSON_OBJECT('mode','rate','base','salary','value',0.09), 'DRAFT',     NULL,                  NULL,                    1, NOW(), 1, NOW(), 0, 0),
(@ct_3, 'RATE', 1, '2025-07-01', JSON_OBJECT('mode','fixed','base','manhour','value',35), 'DISABLED',  '2025-07-02 09:00:00', '2026-01-01 10:30:00', 1, NOW(), 1, NOW(), 0, 0),
(@ct_3, 'RATE', 2, '2026-01-01', JSON_OBJECT('mode','fixed','base','manhour','value',38), 'PUBLISHED', '2026-01-02 09:00:00', NULL,                    1, NOW(), 1, NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
`rule_type`       = VALUES(`rule_type`),
`effective_from`  = VALUES(`effective_from`),
`rule_payload`    = VALUES(`rule_payload`),
`status`          = VALUES(`status`),
`published_at`    = VALUES(`published_at`),
`deactivated_at`  = VALUES(`deactivated_at`),
`update_user`     = 1,
`update_time`     = NOW(),
`deleted`         = 0,
`tenant_id`       = 0;

COMMIT;
