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

-- changeset labor:5-phase3-schema
-- comment Add workforce and attendance tables for labor phase 03
CREATE TABLE IF NOT EXISTS `labor_employee_profile` (
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `employee_no` varchar(64)  NOT NULL,
    `name`        varchar(128) NOT NULL,
    `id_no`       varchar(64)  DEFAULT NULL,
    `phone`       varchar(32)  DEFAULT NULL,
    `dept_id`     bigint(20)   DEFAULT NULL,
    `status`      varchar(32)  NOT NULL,
    `hired_at`    date         DEFAULT NULL,
    `offboard_at` date         DEFAULT NULL,
    `tenant_id`   bigint(20)   NOT NULL DEFAULT 0,
    `create_user` bigint(20)   NOT NULL,
    `create_time` datetime     NOT NULL,
    `update_user` bigint(20)   DEFAULT NULL,
    `update_time` datetime     DEFAULT NULL,
    `deleted`     bigint(20)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_employee_no` (`employee_no`, `deleted`, `tenant_id`),
    INDEX `idx_employee_status` (`status`),
    INDEX `idx_employee_dept_id` (`dept_id`),
    INDEX `idx_employee_tenant_id` (`tenant_id`),
    INDEX `idx_employee_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Labor employee profile';

CREATE TABLE IF NOT EXISTS `labor_employee_bank_account` (
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT,
    `employee_id`  bigint(20)   NOT NULL,
    `account_name` varchar(128) NOT NULL,
    `bank_name`    varchar(128) NOT NULL,
    `bank_no`      varchar(128) NOT NULL,
    `is_default`   bit(1)       NOT NULL DEFAULT b'0',
    `tenant_id`    bigint(20)   NOT NULL DEFAULT 0,
    `create_user`  bigint(20)   NOT NULL,
    `create_time`  datetime     NOT NULL,
    `update_user`  bigint(20)   DEFAULT NULL,
    `update_time`  datetime     DEFAULT NULL,
    `deleted`      bigint(20)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_bank_employee_id` (`employee_id`),
    INDEX `idx_bank_tenant_id` (`tenant_id`),
    INDEX `idx_bank_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Labor employee bank account';

CREATE TABLE IF NOT EXISTS `labor_contract_assignment` (
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT,
    `contract_id`   bigint(20)   NOT NULL,
    `employee_id`   bigint(20)   NOT NULL,
    `position_name` varchar(128) DEFAULT NULL,
    `level_name`    varchar(64)  DEFAULT NULL,
    `assigned_at`   date         NOT NULL,
    `unassigned_at` date         DEFAULT NULL,
    `status`        varchar(32)  NOT NULL DEFAULT 'ACTIVE',
    `tenant_id`     bigint(20)   NOT NULL DEFAULT 0,
    `create_user`   bigint(20)   NOT NULL,
    `create_time`   datetime     NOT NULL,
    `update_user`   bigint(20)   DEFAULT NULL,
    `update_time`   datetime     DEFAULT NULL,
    `deleted`       bigint(20)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_assignment_contract_employee_assigned` (`contract_id`, `employee_id`, `assigned_at`, `deleted`, `tenant_id`),
    INDEX `idx_assignment_status` (`status`),
    INDEX `idx_assignment_employee` (`employee_id`),
    INDEX `idx_assignment_tenant_id` (`tenant_id`),
    INDEX `idx_assignment_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Labor contract assignment';

CREATE TABLE IF NOT EXISTS `labor_shift` (
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `contract_id` bigint(20)   NOT NULL,
    `shift_name`  varchar(128) NOT NULL,
    `start_time`  time         NOT NULL,
    `end_time`    time         NOT NULL,
    `tenant_id`   bigint(20)   NOT NULL DEFAULT 0,
    `create_user` bigint(20)   NOT NULL,
    `create_time` datetime     NOT NULL,
    `update_user` bigint(20)   DEFAULT NULL,
    `update_time` datetime     DEFAULT NULL,
    `deleted`     bigint(20)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_shift_contract_id` (`contract_id`),
    INDEX `idx_shift_tenant_id` (`tenant_id`),
    INDEX `idx_shift_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Labor shift';

CREATE TABLE IF NOT EXISTS `labor_attendance_record` (
    `id`               bigint(20) NOT NULL AUTO_INCREMENT,
    `contract_id`      bigint(20) NOT NULL,
    `employee_id`      bigint(20) NOT NULL,
    `work_date`        date       NOT NULL,
    `shift_id`         bigint(20) NOT NULL,
    `check_in_at`      datetime   DEFAULT NULL,
    `check_out_at`     datetime   DEFAULT NULL,
    `work_minutes`     int        NOT NULL DEFAULT 0,
    `overtime_minutes` int        NOT NULL DEFAULT 0,
    `status`           varchar(32) NOT NULL,
    `tenant_id`        bigint(20) NOT NULL DEFAULT 0,
    `create_user`      bigint(20) NOT NULL,
    `create_time`      datetime   NOT NULL,
    `update_user`      bigint(20) DEFAULT NULL,
    `update_time`      datetime   DEFAULT NULL,
    `deleted`          bigint(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_attendance_employee_day_shift` (`employee_id`, `work_date`, `shift_id`, `deleted`, `tenant_id`),
    INDEX `idx_attendance_contract_id` (`contract_id`),
    INDEX `idx_attendance_status` (`status`),
    INDEX `idx_attendance_tenant_id` (`tenant_id`),
    INDEX `idx_attendance_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Labor attendance record';

CREATE TABLE IF NOT EXISTS `labor_attendance_correction` (
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT,
    `attendance_id` bigint(20)  NOT NULL,
    `employee_id`  bigint(20)   NOT NULL,
    `reason`       varchar(512) NOT NULL,
    `status`       varchar(32)  NOT NULL,
    `reviewed_by`  bigint(20)   DEFAULT NULL,
    `reviewed_at`  datetime     DEFAULT NULL,
    `tenant_id`    bigint(20)   NOT NULL DEFAULT 0,
    `create_user`  bigint(20)   NOT NULL,
    `create_time`  datetime     NOT NULL,
    `update_user`  bigint(20)   DEFAULT NULL,
    `update_time`  datetime     DEFAULT NULL,
    `deleted`      bigint(20)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_correction_attendance_id` (`attendance_id`),
    INDEX `idx_correction_employee_id` (`employee_id`),
    INDEX `idx_correction_status` (`status`),
    INDEX `idx_correction_tenant_id` (`tenant_id`),
    INDEX `idx_correction_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Labor attendance correction';
