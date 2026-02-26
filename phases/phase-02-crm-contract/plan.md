# Phase 02: 客户与合同管理

## 范围
- 前端：客户线索列表与详情、用工单位管理、合同列表与详情、结算规则配置页面。
- 后端：客户线索管理、用工单位管理、合同生命周期（签署/续签/终止）、结算规则版本管理。
- 数据库：`customer_lead`、`employer_unit`、`labor_contract`、`settlement_rule`。

## 交付目标
交付“线索 -> 用工单位 -> 合同 -> 结算规则”的业务闭环，确保状态流转可追踪、规则配置可复用，为后续考勤与结算阶段提供稳定输入。

## 验收标准
1. 线索支持创建、跟进、状态流转，且可审计关键变更。
2. 合同支持签署、续签、终止流程，生命周期状态准确。
3. 结算规则支持版本化管理，版本与生效条件可回溯。
4. 前端可完成核心业务操作并通过至少 1 条端到端链路验证。

## 演进与同步
- Schema：先提交 migration，再同步 `docs/database-schema.md`。
- API：新增或变更接口后同步更新 `docs/api-contracts.md`。
- 架构：若模块边界、领域关系调整，更新 `docs/architecture.md`。
- 基础设施：本阶段不新增中间件，沿用现有 `docker-compose.dev.yml`。

## 参考
- `docs/phase-plan.md`
- `docs/specification.md`
- `docs/architecture.md`
- `docs/api-contracts.md`
- `docs/database-schema.md`
