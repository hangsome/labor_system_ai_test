# 系统架构设计（Stage 1）

## 1. 系统概述

劳务系统是面向酒店等用工行业的企业管理平台，覆盖从客户线索、合同签约、员工管理、考勤结算到发票回款的业务闭环。  
本期采用“模块化单体 + 事件驱动扩展”架构，先保障交付速度与业务一致性，在并发和组织规模增长时演进为服务化。

## 2. 模块划分

### 2.1 模块总览

| 模块名 | 职责 | 类型 | 依赖模块 |
|---|---|---|---|
| iam | 登录、认证、RBAC、数据权限 | 全栈 | - |
| platform | 字典、系统配置、审计日志、通知 | 后端 | iam |
| crm | 客户线索、用工单位、项目基础资料 | 全栈 | iam, platform |
| contract | 合同管理、条款、附件、续签与终止 | 全栈 | crm, iam |
| workforce | 员工档案、入离职、银行账户、保险 | 全栈 | iam, platform |
| attendance | 排班、打卡、补卡审批、考勤台账 | 全栈 | workforce, contract |
| settlement | 结算规则、结算单、明细、调整单 | 全栈 | attendance, contract |
| billing | 支付批次、打款记录、幂等控制 | 全栈 | settlement, iam |
| finance | 发票申请、应收台账、核销、资金看板 | 全栈 | billing, settlement |
| training | 培训计划、学习进度、课件资源 | 全栈 | workforce |
| performance | KPI/行为考核、评分流程、结果汇总 | 全栈 | workforce, training |
| points | 积分规则、积分流水、排行与激励 | 全栈 | workforce, performance |

### 2.2 模块依赖图（文字）

1. `iam` 与 `platform` 是底座模块，其他模块统一复用。  
2. `crm -> contract -> attendance -> settlement -> billing -> finance` 构成核心财务主链路。  
3. `workforce` 作为人力基础，被 `attendance/training/performance/points` 依赖。  
4. 跨域数据同步优先通过领域事件（RabbitMQ）异步传播，避免强耦合直连。  
5. 禁止跨模块直接访问 Repository，只允许通过 Application Service 或 Domain Service。

### 2.3 本阶段范围标注

1. 本阶段（Stage 1）已完整覆盖核心主链路：`iam/crm/contract/workforce/attendance/settlement/billing/finance`。  
2. `training/performance/points` 在架构层已定义边界，但数据库与 API 细化延后到 Stage 2 的 Phase 划分后落地。  
3. 为避免歧义，后续文档若未覆盖扩展模块，统一标记为 `Out of Scope (Stage 1)`。

## 3. 技术架构

### 3.1 前端架构

- 技术栈：Vue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus。
- 路由分层：
  - `/login`：认证入口
  - `/dashboard`：管理驾驶舱
  - `/crm/*`：客户与线索
  - `/contract/*`：合同与结算规则
  - `/workforce/*`：员工与组织
  - `/attendance/*`：排班与考勤
  - `/settlement/*`：结算与支付
  - `/finance/*`：发票、应收与资金
  - `/system/*`：角色权限与系统配置
- 状态管理：
  - `authStore`：令牌、用户信息、权限点
  - `appStore`：全局字典、租户与主题信息
  - `domainStore`：各业务域按需拆分，避免超大 store
- 组件规范：
  - 页面容器（Page）+ 业务组件（Biz）+ 通用组件（Common）
  - 表格、表单、筛选器统一抽象，减少页面重复实现

### 3.2 后端架构

- 技术栈：Spring Boot 3.3 + Spring Security + Spring Data JPA + Flyway + Redis + RabbitMQ。
- 分层设计：
  - `controller`：接口适配与参数校验
  - `application`：用例编排、事务边界
  - `domain`：核心业务规则
  - `infrastructure`：持久化、消息、外部适配
- 横切能力：
  - 统一异常处理器（错误码 + traceId）
  - 审计日志切面（关键写操作全量记录）
  - 数据权限拦截器（按 `ALL/DEPT/PROJECT/CLIENT/SELF` 注入过滤）
  - 幂等组件（`X-Idempotency-Key`）
- 日志规范：
  - JSON 结构化日志，字段包含 `traceId/userId/module/action/result`
  - 敏感字段（手机号、身份证、银行卡）日志脱敏

### 3.3 通信方式

- 前后端：RESTful JSON API（`/api/admin/v1`）。
- 鉴权：JWT Access Token + Refresh Token。
- 同步调用：HTTP（内部模块通过 Service 调用，不通过 HTTP 回环）。
- 异步事件（RabbitMQ Topic）：
  - `settlement.completed`
  - `payment.status.changed`
  - `attendance.abnormal`
  - `invoice.status.changed`
  - `audit.action`

## 4. 安全设计

- 认证：JWT + Refresh；短期 access token，支持主动失效（Redis 黑名单）。
- 授权：RBAC + 数据权限（行级过滤）。
- 输入校验：前端表单校验 + 后端 Bean Validation 双重校验。
- 幂等与防重：关键写接口必须携带 `X-Idempotency-Key`。
- 数据安全：
  - 传输层 TLS
  - 敏感字段加密或脱敏（身份证、手机号、银行卡号）
  - 附件访问 URL 签名并设置时效
- 审计与追溯：关键业务动作写入 `audit_log`，支持按对象和操作人检索。

### 4.1 Phase 1 已落地安全边界（2026-02-26）

- 认证边界：
  - `/api/admin/v1/auth/login`、`/refresh`、`/me` 已落地。
  - token 机制支持 access/refresh 与主动失效。
- 授权边界：
  - 角色权限接口已落地：`/api/admin/v1/iam/roles`、`/roles/{roleId}/permissions`。
  - 文档兼容路由补齐：`/api/admin/v1/system/roles`、`/system/permissions`。
- 数据权限边界：
  - 数据权限拦截器已注入 `/api/admin/v1/system/**` 链路。
  - 支持 `ALL/DEPT/SELF/NONE` 策略，默认拒绝越权访问。
- 审计边界：
  - 关键写操作通过 `@AuditAction` 记录审计日志。
  - 查询接口 `/api/admin/v1/platform/audit-logs` 已支持按 `bizType/bizId/operatorId` 筛选。

## 5. 非功能需求落地

- 性能目标：
  - API P95 < 300ms
  - API P99 < 800ms
  - 首屏加载 < 2.5s
- 可用性：
  - 目标可用率 >= 99.9%
  - 关键任务支持失败重试与死信队列
- 可扩展性：
  - 当前为模块化单体
  - 满足以下任一条件触发服务化评估：连续 2 周 P99 > 800ms、核心队列积压 > 10 分钟、并发 > 1 万

## 6. 技术风险

| 风险 | 影响 | 缓解措施 |
|---|---|---|
| Java 基线一致性 | Java 17 与 Java 21 并存策略若不清晰会导致环境分歧 | 2026-02-26 起统一以 Java 17 作为开发与 CI 基线，后续按阶段评估升级 21 |
| 本地依赖容器可用性 | 若 Docker 依赖未启动，集成链路验证会中断 | 2026-02-26 已完成 `docker compose up -d` 并通过健康检查（MySQL/Redis/RabbitMQ） |
| 多来源 PRD 版本不一致 | 需求理解漂移，接口反复调整 | Stage 2 固化 PRD 主版本清单并建立变更登记 |
| 结算规则复杂度高 | 计算口径错误会直接影响资金准确性 | 在 Stage 3 强制增加契约测试与对账测试任务 |
| 数据权限策略遗漏 | 越权查询导致安全问题 | 接口层与仓储层双重校验 + 安全用例回归 |

## 7. Pattern Library 使用情况

- 已检查项目根目录 `patterns/`：当前不存在可复用 Pattern。
- 本阶段采用标准模式作为初始基线：
  - RBAC + DataScope
  - JWT + Refresh
  - 模块化单体 + 事件解耦
