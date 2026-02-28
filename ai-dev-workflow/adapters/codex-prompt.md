---
description: Codex 适配指南 — 在 IDE / CLI / AGENTS / Skill 中正确使用 AI Dev Workflow v1.2（7 Stage）
---

# Codex 使用指南

本文件用于指导你在 Codex 中正确运行 `ai-dev-workflow`。

适配版本：
- Workflow: `v1.2`
- Stage: `7 Stage (0-6)`

## 1. 推荐接入方式

### 方式 A：Skill（推荐长期项目）

适用：团队协作、重复使用同一工作流。

关键目录：

```text
<project-root>/
├── .agents/
│   └── skills/
│       └── dev-workflow/
│           └── SKILL.md
└── ai-dev-workflow/
    ├── workflow-main.md
    ├── agent-routing.md
    ├── stages/
    │   ├── stage-0-project-init.md
    │   ├── stage-1-architecture.md
    │   ├── stage-2-phase-planning.md
    │   ├── stage-3-task-decomposition.md
    │   ├── stage-4-execution.md
    │   ├── stage-5-review-handoff.md
    │   └── stage-6-deployment.md
    └── templates/
```

启动示例：

```text
$dev-workflow 开发一个企业级 CRM 系统，包含客户管理、销售漏斗、权限管理。
```

### 方式 B：AGENTS.md（推荐项目级默认启用）

适用：希望仓库内所有会话默认遵循该流程。

在仓库根 `AGENTS.md` 声明：

```markdown
- 主流程：ai-dev-workflow/workflow-main.md
- Stage 顺序：0 -> 1 -> 2 -> [3 -> 4 -> 5] 循环 -> 6
- 恢复方式：新会话输入“继续”
```

### 方式 C：CLI `@file` 临时引用（快速试跑）

适用：不想改任何配置，仅临时执行。

```bash
codex
# 在会话中输入
请读取 @ai-dev-workflow/workflow-main.md 并按 7 Stage 执行，需求如下：...
```

## 2. 正确使用姿势（Codex）

### 新项目启动

```text
请读取 ai-dev-workflow/workflow-main.md 并严格按 7 Stage 工作流执行。
需求：<你的需求>
技术栈偏好：<可选>
```

### 中断恢复

```text
继续
```

### 指定执行当前阶段任务

```text
请读取 @phases/phase-XX/todolist.csv，
并按 @ai-dev-workflow/stages/stage-4-execution.md 执行下一条可执行任务。
```

## 3. 你必须遵守的关键规则

1. 不跳 Stage，主流程固定为 `0 -> 1 -> 2 -> [3 -> 4 -> 5] -> 6`。
2. Stage 4 DoD 与 Stage 5 DoD 分层：
   - Stage 4：`dev_state + test_state + git_state`
   - Stage 5：`review_state + 人工验收`
3. 多 Phase 演进必须同步：
   - Schema 变更 -> 同 Phase migration + `docs/database-schema.md`
   - API/架构变更 -> 同步 `docs/api-contracts.md` / `docs/architecture.md`
   - 新中间件 -> 先更新并验证 `docker-compose.dev.yml`
4. Stage 4 失败回滚优先定向恢复；清理 untracked 仅允许路径限定差集清理，禁止全局 `git clean -fd`。

## 4. 常见误用

1. 把工作流说成 6 阶段。
2. 只写代码不更新 CSV/process 状态。
3. 把 migration 和文档同步拖到 Stage 6 才做。
4. 在共享分支执行破坏性清理命令。

## 5. 参考

- `ai-dev-workflow/workflow-main.md`
- `ai-dev-workflow/stages/stage-4-execution.md`
- `ai-dev-workflow/stages/stage-5-review-handoff.md`
- `ai-dev-workflow/stages/stage-6-deployment.md`
