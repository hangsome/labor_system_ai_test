---
description: AI 适配器总览 — 指导用户在 Codex / Claude / Antigravity 中正确使用 dev-workflow
---

# adapters 使用总览

本目录用于说明如何在不同 AI 端正确接入并运行 `ai-dev-workflow`。

当前工作流基线：
- 版本：`v1.2`
- 阶段数：`7 Stage`（Stage 0-6）
- 主入口：`ai-dev-workflow/workflow-main.md`

## 选择建议

| AI 平台 | 推荐用途 | 是否推荐作为主执行引擎 |
|---|---|---|
| Codex | 全流程编排、代码实现、终端执行、状态维护 | 是（推荐） |
| Claude Code | 架构/安全/风险评审，或中小规模项目单体执行 | 条件推荐 |
| Antigravity | 自定义 workflow 引擎场景、企业内部工具链整合 | 条件推荐 |

## 共通前置条件

1. 项目根目录存在 `ai-dev-workflow/`。
2. `ai-dev-workflow/stages/` 包含 `stage-0` 到 `stage-6` 全部定义。
3. 运行期目录允许创建：`docs/`、`phases/`。
4. 若使用多 Agent 协作，需提前约定：
   - Codex：主编排/后端实现
   - Gemini：前端任务（可选）
   - Claude：阻断门审计（Stage 5）

## 快速启动模板

### Codex

```text
请读取 ai-dev-workflow/workflow-main.md 并按 7 Stage 工作流执行。
需求：<你的项目需求>
```

### Claude Code

```text
请读取 ai-dev-workflow/workflow-main.md，按 7 Stage 工作流执行。
若遇到 Stage 5 审计，按 claude-instructions.md 的阻断门规则处理。
需求：<你的项目需求>
```

### Antigravity

```text
请按 ai-dev-workflow/workflow-main.md 与 adapters/antigravity-workflow.md 执行 7 Stage 工作流。
需求：<你的项目需求>
```

## 文档索引

- Codex 适配：`adapters/codex-prompt.md`
- Claude 适配：`adapters/claude-instructions.md`
- Antigravity 适配：`adapters/antigravity-workflow.md`
