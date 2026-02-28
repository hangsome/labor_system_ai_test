---
name: dev-workflow
description: >
  大型系统从零到一的全栈开发工作流（7 Stage 分阶段执行）。
  当用户要求从零开始开发完整系统、需要分阶段规划与多 Agent 协作，
  或提供 plan.md 启动开发时触发。
  不适用于：单文件修改、单个 Bug 修复、代码审查、已有项目小规模迭代。
---

# AI 大型系统开发工作流

你现在进入「AI 大型系统开发工作流」模式。本工作流通过 7 个 Stage 有序推进。

## 恢复检测（首先执行）

启动前检查用户输入和项目状态：
- 若用户输入 `继续` / `yes` / `continue`：扫描 `todolist.csv` + `process.md`，从中断点恢复。
- 若存在 `session-handoff.md`：读取 handoff 并恢复。
- 否则：从 Stage 0 开始。

## 工作流文件引导

按顺序读取：
1. `ai-dev-workflow/workflow-main.md`
2. `ai-dev-workflow/stages/stage-X-xxx.md`
3. `ai-dev-workflow/agent-routing.md`
4. `ai-dev-workflow/templates/`

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

| 任务类型 | 主 Agent | 降级策略 |
|---------|---------|---------|
| 后端开发 | Codex | - |
| 前端开发 | Gemini CLI | 429 -> Claude -> Codex |
| 代码审计 | Claude | 阻断门不可降级 |
| 架构评审 | Claude | 阻断门不可降级 |

## 会话切换

当上下文接近阈值时：
1. 生成 `phases/phase-XX/session-handoff.md`
2. 提交 checkpoint
3. 提示用户在新会话输入“继续”

## 启动

- 提供了 Plan：从 Stage 0 消化 Plan。
- 仅自然语言：先生成 Plan，确认后继续。
