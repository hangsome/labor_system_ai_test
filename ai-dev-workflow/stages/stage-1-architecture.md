---
stage: 1
name: 架构设计
description: 系统分解 → 模块划分 → 数据库设计 → API 合约定义
input: 项目骨架 + 需求描述
output: architecture.md + database-schema.md + api-contracts.md
---

# Stage 1：架构设计

## 一、目标

使用 `sequential-thinking` MCP 对系统进行深入分解，产出完整的架构设计文档，包括模块边界、数据库 Schema 和 API 合约。

## 二、AI 行为约定

1. **必须使用 `sequential-thinking` MCP**（totalThoughts: 8-12）进行系统分解。
2. **不写业务代码**，只输出设计文档。
3. 架构文档必须经过 **Claude 评审**（阻断门），评审通过后才能进入 Stage 2。
4. 数据库设计必须输出可执行的 DDL 语句。
5. 本 Stage 文档是**初始基线**：后续每个 Phase 若发生 Schema/API/架构变更，必须在 Stage 3/5 中增量更新相应文档与 migration。

## 三、执行步骤

### Step 1：系统分解（使用 sequential-thinking）

调用 `sequential-thinking`，建议 10 步思考：

```
thought 1: 业务领域分析 — 提取核心业务实体和业务流程
thought 2: 查阅 Pattern Library — 检查 patterns/ 中是否有可复用的架构模式
thought 3: 模块划分 — 按业务领域划分模块边界（参考已有 Pattern）
thought 4: 模块依赖分析 — 识别模块间依赖和通信模式
thought 5: 数据模型设计 — 核心实体及其关系
thought 6: API 设计原则 — RESTful 规范、版本策略、认证方式
thought 7: 前端架构 — 页面/路由/组件树设计 + 导航闭环分析（确保所有路由有返回路径）
thought 8: 后端分层 — Controller/Service/Repository 分层
thought 9: 安全设计 — 认证、授权、数据加密策略
thought 10: 风险评估 — 技术风险、复杂度评估、缓解措施
```

> 📌 **Pattern 复用规则**：如果 `patterns/` 中存在与当前系统相关的架构模式（如 RBAC、JWT），
> 在 `architecture.md` 中引用该 Pattern 并标注 `ref: patterns/<path>.md`，避免重复设计。

### Step 2：生成 architecture.md

```markdown
# 系统架构设计

## 1. 系统概述
<系统定位、目标用户、核心价值>

## 2. 模块划分

### 2.1 模块总览
| 模块名 | 职责 | 前端/后端/全栈 | 依赖模块 |
|--------|------|---------------|---------|
| 用户模块 | 注册/登录/权限 | 全栈 | — |
| XXX模块 | ... | ... | 用户模块 |

### 2.2 模块依赖图
<文字描述或结构化表示>

## 3. 技术架构

### 3.1 前端架构
- 路由设计（页面列表 + 路由规则）
- 导航闭环矩阵（每个路由页面的入口来源 + 出口返回路径，确保无死胡同）
- 全局导航规范（返回按钮 / 面包屑 / 侧边栏激活态 / 错误页回首页策略）
- 状态管理方案
- 组件设计原则

### 3.2 后端架构
- 分层结构（Controller → Service → Repository → Entity）
- 中间件/拦截器设计
- 异常处理策略
- 日志规范

### 3.3 通信方式
- 前后端：RESTful API (JSON)
- 认证：JWT / Session

## 4. 安全设计
- 认证方式：<JWT / OAuth2>
- 授权模型：<RBAC / ABAC>
- 数据安全：<加密、脱敏>
- 输入校验：<前后端双重校验>

## 5. 非功能需求
- 性能目标：<响应时间、并发量>
- 可用性：<错误处理、降级策略>
- 可扩展性：<水平扩展方案>

## 6. 技术风险
| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| ... | ... | ... |
```

### Step 3：生成 database-schema.md

```markdown
# 数据库设计

## ER 关系说明
<实体及关系的文字描述>

## 表结构

### <表名>
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| ... | ... | ... | ... |

## DDL 语句

CREATE DATABASE IF NOT EXISTS <db_name> DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE <db_name>;

CREATE TABLE <table_name> (
  ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Step 4：生成 api-contracts.md

```markdown
# API 合约

## 全局约定
- Base URL: `/api/v1`
- 认证方式：Bearer Token (JWT)
- 响应格式: `{ "code": 200, "message": "success", "data": {} }`

## 接口列表

### <模块名>

#### <接口名>
- 路径: `<METHOD> /api/v1/<path>`
- 描述: <功能说明>
- 请求参数:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | ... | ... | ... | ... |
- 响应:
  ```json
  { "code": 200, "data": { ... } }
  ```
```

### Step 4.5：生成 test-strategy.md

使用模板 `templates/test-strategy.md`，根据项目的技术栈和 `docs/specification.md` 中的 NFR Matrix 生成测试策略文档。

**输出**：`docs/test-strategy.md`

**关键决策点**：
1. 根据技术栈选择具体测试框架（参考模板的"测试工具推荐"表）
2. 根据 NFR Matrix 中的性能指标设定性能基线
3. 根据系统复杂度调整覆盖率目标（简单系统可降低至 70%/50%）

### Step 5：Claude 评审（阻断门）

1. 将 `architecture.md`、`database-schema.md`、`api-contracts.md` 发送 Claude
2. Claude 评审要点：
   - 模块边界是否清晰
   - 数据库范式是否合理
   - API 设计是否符合 RESTful 规范
   - 安全设计是否完整
   - 是否有明显遗漏
3. 根据 Claude 反馈修改文档
4. 重复直到 Claude 无阻塞意见

## 四、完成条件

- [ ] `docs/architecture.md` 已生成
- [ ] `docs/database-schema.md` 已生成（含可执行 DDL）
- [ ] `docs/api-contracts.md` 已生成
- [ ] Claude 评审通过
- [ ] 用户确认架构方案

## 五、输出到下一 Stage

- 以上三个文档
- 模块列表及其依赖关系（用于 Stage 2 的阶段划分）
