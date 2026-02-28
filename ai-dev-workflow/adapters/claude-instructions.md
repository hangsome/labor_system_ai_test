---
description: Claude Code 适配指南 — 正确使用 AI Dev Workflow v1.2（7 Stage）并处理阻断门审计
---

# Claude Code 使用指南

适配版本：
- Workflow: `v1.2`
- Stage: `7 Stage (0-6)`

## 1. 先选运行模式

### 模式 A：与 Codex 协作（推荐）

适用：商用项目、多人协作、强调审计独立性。

- Codex：主编排与执行（Stage 0/2/3/4/6）
- Claude：审计与评审（重点 Stage 1、Stage 5）
- 人工：最终验收决策

在该模式下，Claude 的主要职责：
1. 架构评审：审查 `docs/architecture.md`、`docs/database-schema.md`、`docs/api-contracts.md`
2. 阶段审计：审查回归结果、文档同步状态、migration 一致性
3. 阻断门判定：给出“通过/不通过 + 风险说明 + 修复建议”

### 模式 B：Claude 单体执行（可用但需声明限制）

适用：个人项目、无 Codex 环境。

- Claude 同时承担编排 + 实现 + 审查角色。
- 由于缺少独立第二审计主体，Stage 5 必须额外引入人工复核。
- 建议在 `notes` 或审查报告标注：`self_review_limited:true`。

## 2. 集成方式

### 方法 1：项目知识注入

```text
/project add ai-dev-workflow/workflow-main.md
/project add ai-dev-workflow/stages/stage-5-review-handoff.md
/project add ai-dev-workflow/agent-routing.md
```

### 方法 2：CLAUDE.md 项目指令（推荐）

在仓库根创建 `CLAUDE.md`：

```markdown
# Claude Project Instructions

本项目使用 AI Dev Workflow v1.2（7 Stage）。

必须先读取：
1. ai-dev-workflow/workflow-main.md
2. ai-dev-workflow/agent-routing.md
3. ai-dev-workflow/stages/stage-0-project-init.md ... stage-6-deployment.md

执行顺序：
Stage 0 -> Stage 1 -> Stage 2 -> [Stage 3 -> Stage 4 -> Stage 5] 循环 -> Stage 6

关键约束：
- Stage 4 DoD: dev/test/git
- Stage 5 DoD: review + 人工验收
- Schema/API/架构/本地依赖必须按 Phase 增量同步
- 中断恢复命令：继续
```

## 3. 标准提示词模板

### 启动新项目

```text
请读取 ai-dev-workflow/workflow-main.md，并按 7 Stage 工作流执行。
需求：<你的需求>
若你处于 Claude 单体模式，请在 Stage 5 标注 self_review_limited 并要求人工复核。
```

### 阶段审计

```text
请按 ai-dev-workflow/stages/stage-5-review-handoff.md 进行本阶段审计，
重点检查：回归失败、migration 连续性、docs 同步一致性、安全风险。
输出通过/不通过结论与修复任务建议。
```

### 中断恢复

```text
继续
```

## 4. 容易出错的点

1. 把流程当 6 阶段使用。
2. 在单体模式下把自审当成独立审计，不要求人工二次复核。
3. 只看代码不看 migration 与文档同步。
4. 阻断门发现问题后不回写任务清单直接放行。

## 5. 参考

- `ai-dev-workflow/workflow-main.md`
- `ai-dev-workflow/stages/stage-5-review-handoff.md`
- `ai-dev-workflow/stages/stage-6-deployment.md`
- `ai-dev-workflow/agent-routing.md`
