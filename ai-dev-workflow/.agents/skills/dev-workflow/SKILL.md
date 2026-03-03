---
name: dev-workflow
description: >
  大型系统从零到一的全栈开发工作流（7 Stage 分阶段执行）。
  支持单 Agent 串行 和 多 Agent 并行 两种模式。
  当用户要求从零开始开发完整系统、需要分阶段规划与多 Agent 协作，
  或提供 plan.md 启动开发，或以角色身份（前端/后端/审计工程师）进入时触发。
  不适用于：单文件修改、单个 Bug 修复、代码审查、已有项目小规模迭代。
---

# AI 大型系统开发工作流

你现在进入「AI 大型系统开发工作流」模式。本工作流支持单 Agent 串行与多 Agent 并行两种模式。

## 角色检测（最先执行 — 多 Agent 模式入口）

检查用户输入是否包含角色关键词，若匹配则进入多 Agent 并行模式：

| 关键词 | 角色 | 执行流 |
|--------|------|--------|
| `前端` / `前端工程师` / `frontend` | Frontend Engineer | 读取 `multi-agent-protocol.md` → Boot Protocol → Stage 4 前端任务 |
| `后端` / `后端工程师` / `backend` | Backend Engineer | 读取 `multi-agent-protocol.md` → Boot Protocol → Stage 4 后端任务 |
| `审计` / `审计工程师` / `audit` / `review` / `审查` | Audit Engineer | 读取 `multi-agent-protocol.md` → Boot Protocol → Stage 5 审计 |
| `编排` / `orchestrator` / `规划` | Orchestrator | 按正常工作流执行（等同无角色关键词） |

**匹配到角色后**：
1. 读取 `ai-dev-workflow/multi-agent-protocol.md`
2. 执行 Boot Protocol（§3）：角色确认 → 项目扫描 → Phase 检测 → 分支切换 → 任务摘要
3. 进入角色对应的 Stage 执行流

**未匹配到角色** → 进入下方的恢复检测和单 Agent 模式。

## 恢复检测（单 Agent 模式入口）

启动前检查用户输入和项目状态：
- 若用户输入 `继续` / `yes` / `continue`：扫描 `todolist.csv` + `process.md`，从中断点恢复。
- 若存在 `session-handoff.md`：读取 handoff 并恢复。
- 否则：从 Stage 0 开始。

## 工作流文件引导

按顺序读取：
1. `ai-dev-workflow/workflow-main.md`
2. `ai-dev-workflow/multi-agent-protocol.md`（多 Agent 模式时优先）
3. `ai-dev-workflow/stages/stage-X-xxx.md`
4. `ai-dev-workflow/agent-routing.md`
5. `ai-dev-workflow/templates/`

## Stage 流转

```text
Stage 0 -> Stage 1 -> Stage 2 -> [Stage 3 -> Stage 4 -> Stage 5] 循环 -> Stage 6 -> 完成
```

| Stage | 名称 | 定义文件 |
|-------|------|---------|
| 0 | 项目初始化 | `stages/stage-0-project-init.md` |
| 1 | 架构设计 | `stages/stage-1-architecture.md` |
| 2 | 阶段规划 | `stages/stage-2-phase-planning.md` |
| 3 | 任务分解 | `stages/stage-3-task-decomposition.md` |
| 4 | 阶段执行 | `stages/stage-4-execution.md` |
| 5 | 审查交接 | `stages/stage-5-review-handoff.md` |
| 6 | 部署与运维就绪 | `stages/stage-6-deployment.md` |

## 核心行为约定

1. 分阶段推进，不跳跃 Stage。
2. 文件即状态源（CSV/Markdown）。
3. 任务执行遵循分层 DoD：Stage 4 达到 `dev_state + test_state + git_state`，Stage 5 达到 `review_state` 并完成人工验收。
4. 回滚优先非破坏性恢复（`git restore`）；共享分支禁止 `git reset --hard`。
5. Stage 5 为人工验收阻断门。
6. 新项目可复用 `patterns/`，每个 Phase 结束后沉淀经验。
7. Schema/API/架构文档与本地基础设施按 Phase 增量演进，禁止集中到 Stage 6 临时补写。

## Agent 路由（快速参考）

### 单 Agent 模式

| 任务类型 | 主 Agent | 降级策略 |
|---------|---------|---------|
| 后端开发 | Codex | - |
| 前端开发 | Gemini CLI | 429 -> Claude -> Codex |
| 代码审计 | Claude | 阻断门不可降级 |
| 架构评审 | Claude | 阻断门不可降级 |

### 多 Agent 模式

| 角色 | 独立会话 | 负责 Stage | Git 分支 |
|------|---------|-----------|---------|
| Orchestrator | Codex / Antigravity | 0/1/2/3/5 决策/6 | main |
| Backend Engineer | Codex / Antigravity | 4（backend 任务） | `feature/phase-XX-<slug>` |
| Frontend Engineer | Gemini / Antigravity | 4（frontend 任务） | `feature/phase-XX-<slug>-fe` |
| Audit Engineer | Claude | 5（代码审计） | 只读 |

## 会话切换

当上下文接近阈值时：
1. 生成 `phases/phase-XX/session-handoff.md`
2. 提交 checkpoint
3. 提示用户在新会话输入"继续"

## 启动

- 提供了 Plan：从 Stage 0 消化 Plan。
- 仅自然语言：先生成 Plan，确认后继续。
- 角色关键词：进入多 Agent 并行模式（按角色执行）。
