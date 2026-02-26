# 数据库设计（Stage 1）

## 1. ER 关系说明

1. `user_account` 与 `role` 多对多，通过 `user_role` 关联。  
2. `role` 与 `permission` 多对多，通过 `role_permission` 关联。  
3. `customer_lead` 可转化为 `employer_unit`（1:N 线索过程，1:1 最终有效转化）。  
4. `labor_contract` 归属 `employer_unit`，并与 `settlement_rule` 为 1:N。  
5. `employee_profile` 与 `contract_assignment` 为 1:N，用于描述员工在合同中的派遣关系。  
6. `attendance_record` 关联 `contract_id + employee_id`，`attendance_correction` 关联 `attendance_record`。  
7. `settlement_order` 与 `settlement_item` 1:N，`payment_batch` 与 `payment_record` 1:N。  
8. `invoice_application` 与 `receivable_ledger` 1:1（V1），`receivable_reconcile` 与 `receivable_ledger` N:1。  
9. `audit_log` 通过 `biz_type + biz_id` 关联业务对象，记录关键动作。

## 2. 表结构（核心）

### 2.1 IAM

#### `user_account`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| username | VARCHAR(64) | UNIQUE, NOT NULL | 登录名 |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希 |
| display_name | VARCHAR(128) | NOT NULL | 显示名 |
| status | VARCHAR(32) | NOT NULL | 状态 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `role`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| code | VARCHAR(64) | UNIQUE, NOT NULL | 角色编码 |
| name | VARCHAR(128) | NOT NULL | 角色名称 |
| created_at | DATETIME | NOT NULL | 创建时间 |

#### `permission`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| code | VARCHAR(128) | UNIQUE, NOT NULL | 权限编码 |
| name | VARCHAR(128) | NOT NULL | 权限名 |
| resource_type | VARCHAR(32) | NOT NULL | MENU/API/BUTTON |
| created_at | DATETIME | NOT NULL | 创建时间 |

#### `data_scope_policy`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| role_id | BIGINT | FK, NOT NULL | 角色ID |
| scope_type | VARCHAR(32) | NOT NULL | ALL/DEPT/PROJECT/CLIENT/SELF |
| scope_ref | VARCHAR(128) | NULL | 作用对象 |
| created_at | DATETIME | NOT NULL | 创建时间 |

### 2.2 客户与合同

#### `customer_lead`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| lead_code | VARCHAR(64) | UNIQUE, NOT NULL | 线索编号 |
| project_name | VARCHAR(255) | NOT NULL | 项目名 |
| contact_name | VARCHAR(64) | NULL | 外部联系人 |
| contact_phone_cipher | VARBINARY(256) | NULL | 加密联系人手机号 |
| industry_type | VARCHAR(32) | NOT NULL | 行业 |
| biz_owner_id | BIGINT | NOT NULL | 业务开发人 |
| cooperation_status | VARCHAR(32) | NOT NULL | 合作状态 |
| tender_at | DATE | NULL | 招标日期 |
| deposit_status | VARCHAR(32) | NOT NULL | 保证金状态 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `employer_unit`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| unit_code | VARCHAR(64) | UNIQUE, NOT NULL | 单位编号 |
| lead_id | BIGINT | FK, NULL | 来源线索 |
| unit_name | VARCHAR(255) | NOT NULL | 单位名称 |
| customer_level | VARCHAR(16) | NOT NULL | 客户等级 |
| address | VARCHAR(512) | NULL | 地址 |
| invoice_info | JSON | NULL | 开票信息 |
| is_outsource | TINYINT | NOT NULL DEFAULT 0 | 是否外包 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `labor_contract`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| contract_no | VARCHAR(128) | UNIQUE, NOT NULL | 合同号 |
| employer_unit_id | BIGINT | FK, NOT NULL | 用工单位ID |
| contract_name | VARCHAR(255) | NOT NULL | 合同名 |
| contract_type | VARCHAR(16) | NOT NULL | A/B |
| start_date | DATE | NOT NULL | 开始日期 |
| end_date | DATE | NOT NULL | 结束日期 |
| settlement_cycle | VARCHAR(32) | NOT NULL | 结算周期 |
| status | VARCHAR(32) | NOT NULL | 合同状态 |
| tax_rate | DECIMAL(6,4) | NOT NULL | 税率 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `settlement_rule`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| contract_id | BIGINT | FK, NOT NULL | 合同ID |
| rule_type | VARCHAR(16) | NOT NULL | HOURLY/DAILY/PIECE/MONTHLY/MIXED |
| version_no | INT | NOT NULL | 版本号 |
| effective_from | DATE | NOT NULL | 生效日 |
| rule_payload | JSON | NOT NULL | 规则参数 |
| created_at | DATETIME | NOT NULL | 创建时间 |

### 2.3 员工与考勤

#### `employee_profile`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| employee_no | VARCHAR(64) | UNIQUE, NOT NULL | 员工编号 |
| name | VARCHAR(128) | NOT NULL | 姓名 |
| id_no_cipher | VARBINARY(512) | NOT NULL | 加密身份证 |
| phone_cipher | VARBINARY(256) | NOT NULL | 加密手机号 |
| dept_id | BIGINT | NULL | 部门ID |
| status | VARCHAR(32) | NOT NULL | ONBOARDING/ACTIVE/OFFBOARD |
| hired_at | DATE | NULL | 入职日期 |
| offboard_at | DATE | NULL | 离职日期 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `contract_assignment`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| contract_id | BIGINT | FK, NOT NULL | 合同ID |
| employee_id | BIGINT | FK, NOT NULL | 员工ID |
| position_name | VARCHAR(128) | NULL | 岗位 |
| level_name | VARCHAR(64) | NULL | 职级 |
| assigned_at | DATE | NOT NULL | 进场日期 |
| unassigned_at | DATE | NULL | 退场日期 |
| status | VARCHAR(32) | NOT NULL | ACTIVE/INACTIVE |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `employee_bank_account`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| employee_id | BIGINT | FK, NOT NULL | 员工ID |
| account_name | VARCHAR(128) | NOT NULL | 开户名 |
| bank_name | VARCHAR(128) | NOT NULL | 开户行 |
| bank_no_cipher | VARBINARY(512) | NOT NULL | 加密银行卡号 |
| is_default | TINYINT | NOT NULL DEFAULT 0 | 是否默认账户 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `shift`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| contract_id | BIGINT | FK, NOT NULL | 合同ID |
| shift_name | VARCHAR(128) | NOT NULL | 班次名称 |
| start_time | TIME | NOT NULL | 上班时间 |
| end_time | TIME | NOT NULL | 下班时间 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `attendance_record`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| contract_id | BIGINT | FK, NOT NULL | 合同ID |
| employee_id | BIGINT | FK, NOT NULL | 员工ID |
| work_date | DATE | NOT NULL | 工作日 |
| shift_id | BIGINT | NOT NULL | 班次ID |
| check_in_at | DATETIME | NULL | 上班时间 |
| check_out_at | DATETIME | NULL | 下班时间 |
| work_minutes | INT | NOT NULL DEFAULT 0 | 出勤分钟 |
| overtime_minutes | INT | NOT NULL DEFAULT 0 | 加班分钟 |
| status | VARCHAR(32) | NOT NULL | NORMAL/ABNORMAL/CORRECTED |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `attendance_correction`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| attendance_id | BIGINT | FK, NOT NULL | 考勤记录ID |
| employee_id | BIGINT | FK, NOT NULL | 申请人 |
| reason | VARCHAR(512) | NOT NULL | 原因 |
| status | VARCHAR(32) | NOT NULL | SUBMITTED/APPROVED/REJECTED/CLOSED |
| reviewed_by | BIGINT | NULL | 审批人 |
| reviewed_at | DATETIME | NULL | 审批时间 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

### 2.4 结算与财务

#### `settlement_order`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| settlement_no | VARCHAR(64) | UNIQUE, NOT NULL | 结算单号 |
| contract_id | BIGINT | FK, NOT NULL | 合同ID |
| cycle_label | VARCHAR(32) | NOT NULL | 结算周期 |
| total_amount | DECIMAL(18,2) | NOT NULL | 总金额 |
| status | VARCHAR(32) | NOT NULL | DRAFT/PENDING_APPROVAL/APPROVED/LOCKED/PAID/CANCELLED |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `settlement_item`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| settlement_order_id | BIGINT | FK, NOT NULL | 结算单ID |
| employee_id | BIGINT | FK, NOT NULL | 员工ID |
| attendance_minutes | INT | NOT NULL | 出勤分钟 |
| overtime_minutes | INT | NOT NULL DEFAULT 0 | 加班分钟 |
| unit_price | DECIMAL(18,4) | NOT NULL | 单价 |
| item_amount | DECIMAL(18,2) | NOT NULL | 金额 |
| overtime_unit_price | DECIMAL(18,4) | NOT NULL DEFAULT 0 | 加班单价 |
| overtime_amount | DECIMAL(18,2) | NOT NULL DEFAULT 0 | 加班金额 |
| created_at | DATETIME | NOT NULL | 创建时间 |

#### `payment_batch`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| batch_no | VARCHAR(64) | UNIQUE, NOT NULL | 批次号 |
| status | VARCHAR(32) | NOT NULL | DRAFT/APPROVED/PAID/FAILED |
| idempotency_key | VARCHAR(128) | UNIQUE, NOT NULL | 幂等键 |
| total_amount | DECIMAL(18,2) | NOT NULL | 批次金额 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `payment_record`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| batch_id | BIGINT | FK, NOT NULL | 批次ID |
| settlement_order_id | BIGINT | FK, NOT NULL | 结算单ID |
| employee_id | BIGINT | FK, NOT NULL | 员工ID |
| paid_amount | DECIMAL(18,2) | NOT NULL | 实付金额 |
| paid_at | DATETIME | NULL | 支付时间 |
| status | VARCHAR(32) | NOT NULL | PENDING/SUCCESS/FAILED |
| created_at | DATETIME | NOT NULL | 创建时间 |

#### `invoice_application`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| apply_no | VARCHAR(64) | UNIQUE, NOT NULL | 申请单号 |
| contract_id | BIGINT | FK, NOT NULL | 合同ID |
| apply_amount | DECIMAL(18,2) | NOT NULL | 申请金额 |
| tax_rate | DECIMAL(6,4) | NOT NULL | 税率 |
| status | VARCHAR(32) | NOT NULL | DRAFT/SUBMITTED/APPROVED/ISSUED/RECONCILED/REJECTED |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `receivable_ledger`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| invoice_application_id | BIGINT | FK, NOT NULL | 发票申请ID |
| receivable_amount | DECIMAL(18,2) | NOT NULL | 应收金额 |
| received_amount | DECIMAL(18,2) | NOT NULL DEFAULT 0 | 已收金额 |
| status | VARCHAR(32) | NOT NULL | OPEN/PARTIAL/CLOSED |
| due_date | DATE | NULL | 到期日 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### `receivable_reconcile`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| ledger_id | BIGINT | FK, NOT NULL | 台账ID |
| reconcile_amount | DECIMAL(18,2) | NOT NULL | 核销金额 |
| reconcile_at | DATETIME | NOT NULL | 核销时间 |
| remark | VARCHAR(255) | NULL | 备注 |
| created_by | BIGINT | NOT NULL | 操作人 |
| created_at | DATETIME | NOT NULL | 创建时间 |

#### `audit_log`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, AI | 主键 |
| biz_type | VARCHAR(64) | NOT NULL | 业务类型 |
| biz_id | VARCHAR(64) | NOT NULL | 业务主键 |
| action | VARCHAR(64) | NOT NULL | 操作 |
| operator_id | BIGINT | NOT NULL | 操作人 |
| trace_id | VARCHAR(64) | NOT NULL | 链路ID |
| detail | JSON | NULL | 详情 |
| created_at | DATETIME | NOT NULL | 创建时间 |

## 3. DDL 语句（可执行）

### 3.0 Phase 01 迁移对齐说明

1. `V1__baseline.sql` 提供最小可运行基线（仅 `user_account` 核心字段）。  
2. `V2__phase01_iam_platform_baseline.sql` 对齐 Phase 01 任务，补齐 `role/permission/role_permission/user_role/data_scope_policy/audit_log`。  
3. `V2` 同时对 `user_account` 执行幂等补列（`display_name`、`updated_at`），兼容已初始化环境。  
4. 迁移已在本地 MySQL (`root`) 与开发容器环境完成可执行验证。  

```sql
CREATE DATABASE IF NOT EXISTS labor_system
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE labor_system;

CREATE TABLE IF NOT EXISTS user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(128) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  resource_type VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS role_permission (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES role(id),
  CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES user_account(id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS data_scope_policy (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  scope_type VARCHAR(32) NOT NULL,
  scope_ref VARCHAR(128) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_data_scope_policy_role FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
  CONSTRAINT fk_settlement_rule_contract FOREIGN KEY (contract_id) REFERENCES labor_contract(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS employee_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_no VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  id_no_cipher VARBINARY(512) NOT NULL,
  phone_cipher VARBINARY(256) NOT NULL,
  dept_id BIGINT NULL,
  status VARCHAR(32) NOT NULL,
  hired_at DATE NULL,
  offboard_at DATE NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS contract_assignment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  contract_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  position_name VARCHAR(128) NULL,
  level_name VARCHAR(64) NULL,
  assigned_at DATE NOT NULL,
  unassigned_at DATE NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_assignment_contract_employee_active (contract_id, employee_id, assigned_at),
  CONSTRAINT fk_contract_assignment_contract FOREIGN KEY (contract_id) REFERENCES labor_contract(id),
  CONSTRAINT fk_contract_assignment_employee FOREIGN KEY (employee_id) REFERENCES employee_profile(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS employee_bank_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_id BIGINT NOT NULL,
  account_name VARCHAR(128) NOT NULL,
  bank_name VARCHAR(128) NOT NULL,
  bank_no_cipher VARBINARY(512) NOT NULL,
  is_default TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_employee_bank_account_employee_id (employee_id),
  CONSTRAINT fk_employee_bank_account_employee FOREIGN KEY (employee_id) REFERENCES employee_profile(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS shift (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  contract_id BIGINT NOT NULL,
  shift_name VARCHAR(128) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_shift_contract_id (contract_id),
  CONSTRAINT fk_shift_contract FOREIGN KEY (contract_id) REFERENCES labor_contract(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS attendance_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  contract_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  work_date DATE NOT NULL,
  shift_id BIGINT NOT NULL,
  check_in_at DATETIME NULL,
  check_out_at DATETIME NULL,
  work_minutes INT NOT NULL DEFAULT 0,
  overtime_minutes INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_attendance_employee_day_shift (employee_id, work_date, shift_id),
  CONSTRAINT fk_attendance_record_contract FOREIGN KEY (contract_id) REFERENCES labor_contract(id),
  CONSTRAINT fk_attendance_record_employee FOREIGN KEY (employee_id) REFERENCES employee_profile(id),
  CONSTRAINT fk_attendance_record_shift FOREIGN KEY (shift_id) REFERENCES shift(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS attendance_correction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  attendance_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  reason VARCHAR(512) NOT NULL,
  status VARCHAR(32) NOT NULL,
  reviewed_by BIGINT NULL,
  reviewed_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_attendance_correction_attendance FOREIGN KEY (attendance_id) REFERENCES attendance_record(id),
  CONSTRAINT fk_attendance_correction_employee FOREIGN KEY (employee_id) REFERENCES employee_profile(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS settlement_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  settlement_no VARCHAR(64) NOT NULL UNIQUE,
  contract_id BIGINT NOT NULL,
  cycle_label VARCHAR(32) NOT NULL,
  total_amount DECIMAL(18,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_settlement_contract_cycle (contract_id, cycle_label),
  CONSTRAINT fk_settlement_order_contract FOREIGN KEY (contract_id) REFERENCES labor_contract(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS settlement_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  settlement_order_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  attendance_minutes INT NOT NULL,
  overtime_minutes INT NOT NULL DEFAULT 0,
  unit_price DECIMAL(18,4) NOT NULL,
  item_amount DECIMAL(18,2) NOT NULL,
  overtime_unit_price DECIMAL(18,4) NOT NULL DEFAULT 0,
  overtime_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_settlement_item_order FOREIGN KEY (settlement_order_id) REFERENCES settlement_order(id),
  CONSTRAINT fk_settlement_item_employee FOREIGN KEY (employee_id) REFERENCES employee_profile(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_batch (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  batch_no VARCHAR(64) NOT NULL UNIQUE,
  status VARCHAR(32) NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL UNIQUE,
  total_amount DECIMAL(18,2) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  batch_id BIGINT NOT NULL,
  settlement_order_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  paid_amount DECIMAL(18,2) NOT NULL,
  paid_at DATETIME NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_payment_record_batch FOREIGN KEY (batch_id) REFERENCES payment_batch(id),
  CONSTRAINT fk_payment_record_settlement_order FOREIGN KEY (settlement_order_id) REFERENCES settlement_order(id),
  CONSTRAINT fk_payment_record_employee FOREIGN KEY (employee_id) REFERENCES employee_profile(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS invoice_application (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  apply_no VARCHAR(64) NOT NULL UNIQUE,
  contract_id BIGINT NOT NULL,
  apply_amount DECIMAL(18,2) NOT NULL,
  tax_rate DECIMAL(6,4) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_invoice_application_contract FOREIGN KEY (contract_id) REFERENCES labor_contract(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS receivable_ledger (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  invoice_application_id BIGINT NOT NULL,
  receivable_amount DECIMAL(18,2) NOT NULL,
  received_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL,
  due_date DATE NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_receivable_ledger_invoice_application FOREIGN KEY (invoice_application_id) REFERENCES invoice_application(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS receivable_reconcile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ledger_id BIGINT NOT NULL,
  reconcile_amount DECIMAL(18,2) NOT NULL,
  reconcile_at DATETIME NOT NULL,
  remark VARCHAR(255) NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_receivable_reconcile_ledger FOREIGN KEY (ledger_id) REFERENCES receivable_ledger(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type VARCHAR(64) NOT NULL,
  biz_id VARCHAR(64) NOT NULL,
  action VARCHAR(64) NOT NULL,
  operator_id BIGINT NOT NULL,
  trace_id VARCHAR(64) NOT NULL,
  detail JSON NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_audit_biz (biz_type, biz_id),
  KEY idx_audit_operator (operator_id),
  KEY idx_audit_created_at (created_at),
  KEY idx_audit_trace_id (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4. 约束与校验规则

1. 金额字段统一 `DECIMAL(18,2)`；税率字段使用 `DECIMAL(6,4)`。  
2. `attendance_record(employee_id, work_date, shift_id)` 强唯一，防止重复打卡。  
3. `settlement_rule(contract_id, version_no)` 强唯一，防止规则版本冲突。  
4. `settlement_order(contract_id, cycle_label)` 强唯一，防止重复生成同周期结算单。  
5. 核销累计金额不得大于应收金额（应用层 + SQL 事务双重校验）。  
6. 结算单非 `APPROVED/LOCKED` 状态不得进入支付批次。  
7. 所有关键写操作必须落审计日志。  
8. 联系人手机号、员工手机号、银行卡号均使用密文字段存储。  
9. `training/performance/points` 模块表结构标记为 `Out of Scope (Stage 1)`，在 Stage 2 Phase 划分后增量补齐。
