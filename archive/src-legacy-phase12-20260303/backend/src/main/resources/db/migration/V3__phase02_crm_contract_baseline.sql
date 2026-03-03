-- Phase 02: CRM / Contract baseline schema

CREATE TABLE IF NOT EXISTS customer_lead (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  lead_code VARCHAR(64) NOT NULL UNIQUE,
  project_name VARCHAR(255) NOT NULL,
  contact_name VARCHAR(64) NULL,
  contact_phone_cipher VARBINARY(256) NULL,
  industry_type VARCHAR(32) NOT NULL,
  biz_owner_id BIGINT NOT NULL,
  cooperation_status VARCHAR(32) NOT NULL,
  tender_at DATE NULL,
  deposit_status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_customer_lead_status (cooperation_status),
  KEY idx_customer_lead_owner (biz_owner_id),
  KEY idx_customer_lead_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS employer_unit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  unit_code VARCHAR(64) NOT NULL UNIQUE,
  lead_id BIGINT NULL,
  unit_name VARCHAR(255) NOT NULL,
  customer_level VARCHAR(16) NOT NULL,
  address VARCHAR(512) NULL,
  invoice_info JSON NULL,
  is_outsource TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_employer_unit_lead_id (lead_id),
  KEY idx_employer_unit_name (unit_name),
  CONSTRAINT fk_employer_unit_lead FOREIGN KEY (lead_id) REFERENCES customer_lead(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS labor_contract (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  contract_no VARCHAR(128) NOT NULL UNIQUE,
  employer_unit_id BIGINT NOT NULL,
  contract_name VARCHAR(255) NOT NULL,
  contract_type VARCHAR(16) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  settlement_cycle VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  tax_rate DECIMAL(6,4) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_labor_contract_employer_id (employer_unit_id),
  KEY idx_labor_contract_status (status),
  KEY idx_labor_contract_date_range (start_date, end_date),
  CONSTRAINT fk_labor_contract_employer_unit FOREIGN KEY (employer_unit_id) REFERENCES employer_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS settlement_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  contract_id BIGINT NOT NULL,
  rule_type VARCHAR(16) NOT NULL,
  version_no INT NOT NULL,
  effective_from DATE NOT NULL,
  rule_payload JSON NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_rule_contract_version (contract_id, version_no),
  KEY idx_settlement_rule_effective_from (effective_from),
  CONSTRAINT fk_settlement_rule_contract FOREIGN KEY (contract_id) REFERENCES labor_contract(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
