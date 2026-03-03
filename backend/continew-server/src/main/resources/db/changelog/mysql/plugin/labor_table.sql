-- liquibase formatted sql

-- changeset labor:1
-- comment Initialize labor domain tables
CREATE TABLE IF NOT EXISTS `labor_lead` (
    `id`                 bigint(20)   NOT NULL AUTO_INCREMENT,
    `lead_code`          varchar(64)  NOT NULL,
    `project_name`       varchar(255) NOT NULL,
    `contact_name`       varchar(64)  DEFAULT NULL,
    `contact_phone`      varchar(64)  DEFAULT NULL,
    `industry_type`      varchar(32)  NOT NULL,
    `biz_owner_id`       bigint(20)   NOT NULL,
    `cooperation_status` varchar(32)  NOT NULL,
    `tender_at`          date         DEFAULT NULL,
    `deposit_status`     varchar(32)  NOT NULL,
    `create_user`        bigint(20)   NOT NULL,
    `create_time`        datetime     NOT NULL,
    `update_user`        bigint(20)   DEFAULT NULL,
    `update_time`        datetime     DEFAULT NULL,
    `deleted`            bigint(20)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_lead_code` (`lead_code`, `deleted`),
    INDEX `idx_lead_owner` (`biz_owner_id`),
    INDEX `idx_lead_status` (`cooperation_status`),
    INDEX `idx_lead_update_time` (`update_time`),
    INDEX `idx_lead_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Labor lead';

CREATE TABLE IF NOT EXISTS `labor_lead_follow_up` (
    `id`              bigint(20)   NOT NULL AUTO_INCREMENT,
    `lead_id`         bigint(20)   NOT NULL,
    `action`          varchar(32)  NOT NULL,
    `content`         varchar(512) DEFAULT NULL,
    `status`          varchar(32)  DEFAULT NULL,
    `status_from`     varchar(32)  DEFAULT NULL,
    `status_to`       varchar(32)  DEFAULT NULL,
    `next_contact_at` datetime     DEFAULT NULL,
    `operator_id`     bigint(20)   NOT NULL DEFAULT 0,
    `create_user`     bigint(20)   NOT NULL,
    `create_time`     datetime     NOT NULL,
    `update_user`     bigint(20)   DEFAULT NULL,
    `update_time`     datetime     DEFAULT NULL,
    `deleted`         bigint(20)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_follow_lead_id` (`lead_id`),
    INDEX `idx_follow_action` (`action`),
    INDEX `idx_follow_create_time` (`create_time`),
    INDEX `idx_follow_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Labor lead follow up';

CREATE TABLE IF NOT EXISTS `labor_employer` (
    `id`             bigint(20)   NOT NULL AUTO_INCREMENT,
    `unit_code`      varchar(64)  NOT NULL,
    `lead_id`        bigint(20)   DEFAULT NULL,
    `unit_name`      varchar(255) NOT NULL,
    `customer_level` varchar(16)  NOT NULL,
    `address`        varchar(512) DEFAULT NULL,
    `invoice_info`   json         DEFAULT NULL,
    `is_outsource`   bit(1)       NOT NULL DEFAULT b'0',
    `create_user`    bigint(20)   NOT NULL,
    `create_time`    datetime     NOT NULL,
    `update_user`    bigint(20)   DEFAULT NULL,
    `update_time`    datetime     DEFAULT NULL,
    `deleted`        bigint(20)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_employer_unit_code` (`unit_code`, `deleted`),
    INDEX `idx_employer_lead_id` (`lead_id`),
    INDEX `idx_employer_name` (`unit_name`),
    INDEX `idx_employer_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Labor employer';

CREATE TABLE IF NOT EXISTS `labor_contract` (
    `id`               bigint(20)    NOT NULL AUTO_INCREMENT,
    `contract_no`      varchar(128)  NOT NULL,
    `employer_unit_id` bigint(20)    NOT NULL,
    `contract_name`    varchar(255)  NOT NULL,
    `contract_type`    varchar(16)   NOT NULL,
    `start_date`       date          NOT NULL,
    `end_date`         date          NOT NULL,
    `settlement_cycle` varchar(32)   NOT NULL,
    `status`           varchar(32)   NOT NULL,
    `tax_rate`         decimal(6, 4) NOT NULL,
    `create_user`      bigint(20)    NOT NULL,
    `create_time`      datetime      NOT NULL,
    `update_user`      bigint(20)    DEFAULT NULL,
    `update_time`      datetime      DEFAULT NULL,
    `deleted`          bigint(20)    NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_contract_no` (`contract_no`, `deleted`),
    INDEX `idx_contract_employer` (`employer_unit_id`),
    INDEX `idx_contract_status` (`status`),
    INDEX `idx_contract_date` (`start_date`, `end_date`),
    INDEX `idx_contract_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Labor contract';

CREATE TABLE IF NOT EXISTS `labor_settlement_rule` (
    `id`             bigint(20)   NOT NULL AUTO_INCREMENT,
    `contract_id`    bigint(20)   NOT NULL,
    `rule_type`      varchar(16)  NOT NULL,
    `version_no`     int          NOT NULL,
    `effective_from` date         NOT NULL,
    `rule_payload`   json         NOT NULL,
    `status`         varchar(16)  NOT NULL DEFAULT 'DRAFT',
    `published_at`   datetime     DEFAULT NULL,
    `deactivated_at` datetime     DEFAULT NULL,
    `create_user`    bigint(20)   NOT NULL,
    `create_time`    datetime     NOT NULL,
    `update_user`    bigint(20)   DEFAULT NULL,
    `update_time`    datetime     DEFAULT NULL,
    `deleted`        bigint(20)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_rule_contract_version` (`contract_id`, `version_no`, `deleted`),
    INDEX `idx_rule_effective_from` (`effective_from`),
    INDEX `idx_rule_status` (`status`),
    INDEX `idx_rule_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Labor settlement rule';
