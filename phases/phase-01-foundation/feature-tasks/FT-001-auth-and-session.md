# Feature Task: FT-001-auth-and-session

> 阶段: Phase 01
> 模块: iam
> 创建时间: 2026-02-26T17:30:00+08:00

---

## 功能概述
建立系统登录与会话基础能力，支持用户名密码登录、令牌刷新、路由鉴权与登录态持久化。
该能力是后续所有业务页面访问控制的前置条件。

## 需求描述

### 功能需求
1. 提供登录、刷新令牌、获取当前用户接口。
2. 提供前端登录页、登录态存储、路由守卫。
3. 登录失败返回统一错误码，成功后按权限控制菜单可见性。

### 非功能需求（如有）
- 性能：登录接口 P95 < 300ms。
- 安全：令牌过期与失效处理明确，敏感字段不明文输出。
- 兼容性：兼容现有 Spring Security 基座。

## 技术设计

### 前端
- 页面/组件：`LoginView`、登录表单组件。
- 路由：补充登录白名单与鉴权守卫。
- 状态管理：`authStore` 管理 access/refresh token 与当前用户。

### 后端
- API 接口：`/api/admin/v1/auth/login`、`/refresh`、`/me`。
- 服务层：认证、令牌签发与刷新逻辑。
- 数据层：用户与角色读取、令牌黑名单存储策略。

### 数据库
- 涉及表：`user_account`、`role`、`permission`。
- 变更类型：新建/基线迁移补齐。

## 子任务映射

| 任务 ID | 标题 | 类型 | Agent |
|---------|------|------|-------|
| PH01-010 | IAM/Platform 基础表迁移 | database | codex |
| PH01-030 | 登录/刷新/当前用户接口 | backend | codex |
| PH01-040 | JWT 刷新与失效处理 | backend | codex |
| PH01-060 | 登录页与表单交互 | frontend | gemini |
| PH01-070 | 登录态与路由守卫 | frontend | gemini |
| PH01-090 | 认证接口集成测试 | test-integration | codex |
| PH01-100 | 登录访问受控菜单 E2E | test-e2e | codex |

## 验收标准

### 前端验收
1. 未登录访问受保护路由会跳转登录页。
2. 登录成功后可进入主框架并显示授权菜单。

### 后端验收
1. 登录成功返回 access/refresh token。
2. refresh token 可换新 access token，失效 token 被拒绝。

### 集成验收
1. 完整链路：登录 -> 刷新 -> 访问当前用户。
2. 错误密码返回统一 401 错误。

## 参考资料

- 架构文档：`docs/architecture.md`
- API 合约：`docs/api-contracts.md`
- 数据库设计：`docs/database-schema.md`
