---
description: 多 Agent 路由规则与降级策略
version: "1.0"
---

# 多 Agent 路由规则

本文件定义了 AI 大型系统开发工作流中多 Agent 的分工、路由和降级策略。

---

## 一、Agent 角色定义

### Codex（主编排 + 后端开发）

**职责**：
- 工作流全局编排（Stage 流转、进度管理）
- 后端代码开发（Java / Python / Node.js）
- 数据库设计与迁移脚本
- API 开发与联调
- 集成测试与 E2E 测试协调
- CSV 状态管理与 process.md 更新

**身份**：主 Agent，所有工作流控制权归 Codex。

### Gemini（前端开发）

**职责**：
- React / Vue 组件开发
- UI/UX 实现（页面布局、样式、动画）
- CSS / Sass / Less 编写
- 前端路由和状态管理
- 前端组件测试

**调用方式**：直接通过本地 Gemini CLI 调用（不依赖 API，使用本地 `gemini` 命令行工具）：
```bash
gemini -p "<task-description>" \
  --sandbox none
```
或以交互模式在项目目录下启动：
```bash
cd <project-root>
gemini
```
> 注意：不使用 `collaborating-with-gemini-cli` skill（它需要 API 接入），而是直接拉起本地已安装的 Gemini CLI。

**降级条件**：Gemini 返回 429 / 限流 / 任意非成功状态码 时，立即降级。

### Claude（审计 + 评审）

**职责**：
- Plan 评审（阻断门 — Stage 1 架构评审必经）
- 代码安全审计（每个 Phase 完成后）
- Code Review（review 类型任务）
- 回归测试审查
- 风险评估与缓解建议

**调用方式**：通过 Claude 子代理（终端或 API）。

**要求**：每次调用 Claude 前，确认其处于 Opus 模式。

---

## 二、路由决策矩阵

### 按任务类型路由

| area | task_type | 主 Agent | 降级 Agent | 备注 |
|------|-----------|---------|-----------|------|
| `backend` | `backend` | Codex | — | 不降级 |
| `backend` | `database` | Codex | — | DDL/迁移 |
| `backend` | `api` | Codex | — | API 开发 |
| `backend` | `config` | Codex | — | 配置文件 |
| `frontend` | `frontend` | Gemini | Claude → Codex | 429 降级链 |
| `frontend` | `test-unit` | Gemini | Claude → Codex | 前端测试 |
| `both` | `test-integration` | Codex | — | 集成测试 |
| `both` | `test-e2e` | Codex | — | E2E 测试 |
| `*` | `review` | Claude | — | 代码审查 |
| `*` | `docs` | Codex | — | 文档更新 |

### 按 Stage 路由

| Stage | 主 Agent | 辅助 Agent |
|-------|---------|-----------|
| Stage 0: 项目初始化 | Codex | — |
| Stage 1: 架构设计 | Codex + sequential-thinking | Claude（评审门） |
| Stage 2: 阶段规划 | Codex | — |
| Stage 3: 任务分解 | Codex | — |
| Stage 4: 执行（后端） | Codex | — |
| Stage 4: 执行（前端） | Gemini | Claude（降级） |
| Stage 4: 执行（审查） | Claude | — |
| Stage 5: 审查交接 | Claude（审计） | Codex（报告） |

---

## 三、降级策略

### Gemini 降级链

```
Gemini (主) 
  ↓ 429/限流/非成功返回码
Claude (第一降级)
  ↓ 不可用
Codex (最终降级，由当前 Agent 直接执行)
```

**降级触发条件**：
- HTTP 状态码 429 (Too Many Requests)
- HTTP 状态码 439 或任何 4xx/5xx
- 超时（默认 1800 秒）
- 网络连接失败

**降级后行为**：
1. 在 CSV 的 `notes` 字段记录：`gemini_fallback:<原因>`
2. 将 `assigned_agent` 更新为实际执行的 Agent
3. **不等待 Gemini 恢复**，立即使用降级 Agent 继续执行
4. 后续同类任务也使用降级 Agent（直到下一次会话开始时重试 Gemini）

### Claude 不可用处理

如果 Claude 不可用，按任务类型分流：

1. **阻断门任务（不可降级）**  
   - 包括：Stage 1 架构评审、Stage 5 发布前审计、所有标记为 `review_gate=blocking` 的任务。  
   - 处理：立即挂起，记录 `claude_unavailable:blocked`，并通知用户转人工审查。  
   - 规则：**禁止**用 Codex 自审代替阻断门。

2. **非阻断审查任务（可降级）**  
   - 可由 Codex 执行临时自审，记录 `claude_unavailable:self_review_non_blocking`。  
   - 后续必须在 Claude 恢复后补一次正式审计并更新结论。

3. **前端任务**  
   - Gemini 不可用且 Claude 也不可用时，允许由 Codex 兜底执行。  
   - 但若该任务属于发布阻断门，仍需等待人工或 Claude 审计通过。

---

## 四、调用记录要求

每次 Agent 调用必须在 `process.md` 的执行日志中记录：

```
<时间> [gemini] 开始执行 PH01-040: 登录页面组件
<时间> [gemini] 返回 429, 降级为 claude
<时间> [claude] 完成 PH01-040, commit: abc1234
```

每次会话结束时，在 `process.md` 底部汇总各 Agent 使用情况：

```
## Agent 使用统计
| Agent | 任务数 | 成功 | 降级 |
|-------|-------|------|------|
| codex | 15 | 15 | 0 |
| gemini | 8 | 6 | 2 |
| claude | 5 | 5 | 0 |
```

---

## 五、Codex 特有规则

作为主编排 Agent，Codex 负责：

1. **流程控制**：决定当前处于哪个 Stage，何时进入 Stage 转换
2. **任务调度**：决定 CSV 中任务的执行顺序
3. **状态管理**：更新 CSV 和 process.md
4. **Agent 调度**：调用 Gemini 和 Claude
5. **异常处理**：处理 Agent 降级、阻塞、失败等异常情况

Codex 不应将以上职责委托给其他 Agent。

---

## 六、跨平台兼容说明

### 在 Antigravity 中

- Antigravity 自身承担 Codex 的角色
- 通过 `browser_subagent` 工具测试前端
- Claude 通过系统内置能力调用
- Gemini 通过本地 Gemini CLI 直接调用

### 在 Claude 中

- Claude 自身承担 Codex + Claude 的角色（自编排 + 自审查）
- Gemini 通过本地 Gemini CLI 直接调用
- 自审查时需在 `notes` 标注 `self_review:true`
