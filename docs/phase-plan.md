# 阶段开发计划（Stage 2）

## 总览

| Phase | 名称 | 模块 | 预估工作量 | 前置依赖 |
|---|---|---|---|---|
| 1 | 基础底座与安全基线 | iam, platform | 2.0 周 | — |
| 2 | 客户与合同主链 | crm, contract | 2.5 周 | Phase 1 |
| 3 | 人员与考勤闭环 | workforce, attendance | 2.5 周 | Phase 1, Phase 2 |
| 4 | 结算支付与财务闭环 | settlement, billing, finance | 3.0 周 | Phase 2, Phase 3 |
| 5 | 扩展域与上线收尾 | training, performance, points, release | 2.0 周 | Phase 1-4 |

## 依赖说明

1. `iam/platform` 是所有业务模块的底座，必须先完成。
2. 核心业务链路遵循 `crm -> contract -> attendance -> settlement -> billing -> finance`。
3. `workforce` 是 `attendance/training/performance/points` 的前置依赖。
4. 扩展域（training/performance/points）在核心资金链路稳定后再落地，降低主线风险。

## Phase 1: 基础底座与安全基线

### 范围
- 前端：登录页、基础布局、系统管理菜单、角色权限管理页面骨架。
- 后端：认证登录、JWT 刷新、RBAC、数据权限策略、审计日志基础能力。
- 数据库：`user_account`、`role`、`permission`、`data_scope_policy`、`audit_log`。

### 交付目标
具备可登录、可鉴权、可审计的最小可用后台基座，后续业务模块可在统一权限框架下并行开发。

### 验收标准
1. 登录/刷新令牌/获取当前用户接口可用，权限校验生效。
2. 至少 1 条关键写操作可在审计日志中追溯。
3. 基础页面可访问且菜单按权限过滤。

### 演进与同步
- Schema 变更：`migration + docs/database-schema.md` 同步。
- API/架构文档：`docs/api-contracts.md`、`docs/architecture.md` 增量更新。
- 本地依赖：无新增中间件，维持现有 `docker-compose.dev.yml`。

## Phase 2: 客户与合同主链

### 范围
- 前端：线索管理、用工单位管理、合同列表/详情、结算规则配置页面。
- 后端：线索 CRUD、线索跟进、用工单位维护、合同创建/续签/终止、结算规则配置。
- 数据库：`customer_lead`、`employer_unit`、`labor_contract`、`settlement_rule`。

### 交付目标
形成“线索 -> 用工单位 -> 合同”业务主链，合同与结算规则可被后续考勤/结算模块复用。

### 验收标准
1. 线索状态流转完整可追踪。
2. 合同生命周期（创建/续签/终止）可闭环执行。
3. 结算规则具备版本化能力并具备唯一性约束。

### 演进与同步
- Schema 变更：`migration + docs/database-schema.md` 同步。
- API/架构文档：`docs/api-contracts.md`、`docs/architecture.md` 增量更新。
- 本地依赖：无新增中间件。

## Phase 3: 人员与考勤闭环

### 范围
- 前端：员工档案、入离职、银行卡管理、排班与考勤台账、补卡审批页面。
- 后端：员工管理、合同派遣、排班导入、考勤记录查询、补卡申请与审批。
- 数据库：`employee_profile`、`contract_assignment`、`employee_bank_account`、`shift`、`attendance_record`、`attendance_correction`。

### 交付目标
形成“员工入场 -> 合同派遣 -> 排班打卡 -> 异常补卡”完整考勤闭环。

### 验收标准
1. 员工在合同下可建立有效派遣关系。
2. 排班与考勤记录可按合同/人员维度查询。
3. 补卡流程具备审批与审计记录。

### 演进与同步
- Schema 变更：`migration + docs/database-schema.md` 同步。
- API/架构文档：`docs/api-contracts.md`、`docs/architecture.md` 增量更新。
- 本地依赖：无新增中间件。

## Phase 4: 结算支付与财务闭环

### 范围
- 前端：结算单、支付批次、发票申请、应收台账与核销页面。
- 后端：结算任务触发、结算审批、支付执行、发票审批、应收核销。
- 数据库：`settlement_order`、`settlement_item`、`payment_batch`、`payment_record`、`invoice_application`、`receivable_ledger`、`receivable_reconcile`。

### 交付目标
形成“考勤数据 -> 结算单 -> 支付 -> 发票 -> 回款核销”资金主链闭环。

### 验收标准
1. 结算单支持生成、审批、调整与状态追踪。
2. 支付批次具备幂等控制并可查询执行结果。
3. 发票与应收核销流程可形成账务闭环。

### 演进与同步
- Schema 变更：`migration + docs/database-schema.md` 同步。
- API/架构文档：`docs/api-contracts.md`、`docs/architecture.md` 增量更新。
- 本地依赖：无新增中间件（继续复用 RabbitMQ/Redis/MySQL/MinIO）。

## Phase 5: 扩展域与上线收尾

### 范围
- 前端：培训、绩效、积分页面；系统配置完善；关键报表与运维页面优化。
- 后端：training/performance/points 业务 API、跨模块数据汇总、上线前配置与脚本。
- 数据库：training/performance/points 相关新表（在本阶段给出最终 schema 与 migration）。

### 交付目标
补齐扩展能力并完成发布前收尾，达到可交付上线状态。

### 验收标准
1. 培训/绩效/积分三域完成最小可用闭环。
2. 关键链路完成回归与性能压测基线校验。
3. 发布文档、部署检查项、回滚策略完整。

### 演进与同步
- Schema 变更：`migration + docs/database-schema.md` 同步（新增扩展域表结构）。
- API/架构文档：`docs/api-contracts.md`、`docs/architecture.md`、必要时 `docs/test-strategy.md` 增量更新。
- 本地依赖：如引入新中间件，必须同步更新 `docker-compose.dev.yml`。

## 当前执行阶段

- 默认从 `Phase 1` 开始进入 Stage 3 任务拆解。
- 若新需求在 Stage 3/4 期间插入，按 Stage 2 动态追加协议进行合并或新增 Phase。
