# Phase 01: 基础底座与安全基线

## 范围
- 前端：登录页、基础布局、系统管理菜单、角色权限管理页面骨架。
- 后端：认证登录、JWT 刷新、RBAC、数据权限策略、审计日志基础能力。
- 数据库：`user_account`、`role`、`permission`、`data_scope_policy`、`audit_log`。

## 交付目标
交付可登录、可鉴权、可审计的最小可用后台基座，作为后续业务 Phase 的统一底座。

## 验收标准
1. 登录/刷新令牌/获取当前用户接口可用，权限校验生效。
2. 至少 1 条关键写操作可在审计日志中追溯。
3. 基础页面可访问且菜单按权限过滤。

## 演进与同步
- Schema：本阶段若新增/调整基础表结构，先落地 migration，再同步 `docs/database-schema.md`。
- API：新增/调整认证与权限接口时，同步更新 `docs/api-contracts.md`。
- 架构：若安全链路或模块边界变化，同步更新 `docs/architecture.md`。
- 基础设施：本阶段不引入新中间件，沿用现有 `docker-compose.dev.yml`。

## 参考
- `docs/phase-plan.md`
- `docs/architecture.md`
- `docs/api-contracts.md`
- `docs/database-schema.md`
