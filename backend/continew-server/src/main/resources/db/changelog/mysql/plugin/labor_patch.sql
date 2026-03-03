-- liquibase formatted sql

-- changeset labor:3
-- comment Localize labor menus and move labor menu right below dashboard
UPDATE `sys_menu` SET `title` = '劳务管理', `sort` = 0 WHERE `id` = 10000;
UPDATE `sys_menu` SET `title` = '线索管理' WHERE `id` = 10100;
UPDATE `sys_menu` SET `title` = '列表' WHERE `id` = 10101;
UPDATE `sys_menu` SET `title` = '详情' WHERE `id` = 10102;
UPDATE `sys_menu` SET `title` = '新增' WHERE `id` = 10103;
UPDATE `sys_menu` SET `title` = '修改' WHERE `id` = 10104;
UPDATE `sys_menu` SET `title` = '删除' WHERE `id` = 10105;
UPDATE `sys_menu` SET `title` = '状态流转' WHERE `id` = 10106;
UPDATE `sys_menu` SET `title` = '跟进列表' WHERE `id` = 10107;
UPDATE `sys_menu` SET `title` = '新增跟进' WHERE `id` = 10108;
UPDATE `sys_menu` SET `title` = '线索详情' WHERE `id` = 10109;
UPDATE `sys_menu` SET `title` = '雇主管理' WHERE `id` = 10200;
UPDATE `sys_menu` SET `title` = '列表' WHERE `id` = 10201;
UPDATE `sys_menu` SET `title` = '详情' WHERE `id` = 10202;
UPDATE `sys_menu` SET `title` = '新增' WHERE `id` = 10203;
UPDATE `sys_menu` SET `title` = '修改' WHERE `id` = 10204;
UPDATE `sys_menu` SET `title` = '删除' WHERE `id` = 10205;
UPDATE `sys_menu` SET `title` = '停用' WHERE `id` = 10206;
UPDATE `sys_menu` SET `title` = '合同管理' WHERE `id` = 10300;
UPDATE `sys_menu` SET `title` = '列表' WHERE `id` = 10301;
UPDATE `sys_menu` SET `title` = '详情' WHERE `id` = 10302;
UPDATE `sys_menu` SET `title` = '新增' WHERE `id` = 10303;
UPDATE `sys_menu` SET `title` = '修改' WHERE `id` = 10304;
UPDATE `sys_menu` SET `title` = '删除' WHERE `id` = 10305;
UPDATE `sys_menu` SET `title` = '签署' WHERE `id` = 10306;
UPDATE `sys_menu` SET `title` = '续签' WHERE `id` = 10307;
UPDATE `sys_menu` SET `title` = '终止' WHERE `id` = 10308;
UPDATE `sys_menu` SET `title` = '合同详情' WHERE `id` = 10309;
UPDATE `sys_menu` SET `title` = '结算管理' WHERE `id` = 10400;
UPDATE `sys_menu` SET `title` = '列表' WHERE `id` = 10401;
UPDATE `sys_menu` SET `title` = '详情' WHERE `id` = 10402;
UPDATE `sys_menu` SET `title` = '新增' WHERE `id` = 10403;
UPDATE `sys_menu` SET `title` = '修改' WHERE `id` = 10404;
UPDATE `sys_menu` SET `title` = '删除' WHERE `id` = 10405;
UPDATE `sys_menu` SET `title` = '发布' WHERE `id` = 10406;
UPDATE `sys_menu` SET `title` = '停用' WHERE `id` = 10407;
UPDATE `sys_menu` SET `title` = '版本' WHERE `id` = 10408;
UPDATE `sys_menu` SET `title` = '生效' WHERE `id` = 10409;

-- changeset labor:4
-- comment Add tenant_id to labor tables to avoid tenant SQL injection errors
ALTER TABLE `labor_lead`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `labor_lead_follow_up`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `labor_employer`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `labor_contract`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);
ALTER TABLE `labor_settlement_rule`
    ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    ADD INDEX `idx_tenant_id` (`tenant_id`);

ALTER TABLE `labor_lead`
    DROP INDEX `uk_lead_code`,
    ADD UNIQUE INDEX `uk_lead_code` (`lead_code`, `deleted`, `tenant_id`);
ALTER TABLE `labor_employer`
    DROP INDEX `uk_employer_unit_code`,
    ADD UNIQUE INDEX `uk_employer_unit_code` (`unit_code`, `deleted`, `tenant_id`);
ALTER TABLE `labor_contract`
    DROP INDEX `uk_contract_no`,
    ADD UNIQUE INDEX `uk_contract_no` (`contract_no`, `deleted`, `tenant_id`);
ALTER TABLE `labor_settlement_rule`
    DROP INDEX `uk_rule_contract_version`,
    ADD UNIQUE INDEX `uk_rule_contract_version` (`contract_id`, `version_no`, `deleted`, `tenant_id`);
