---
description: CrewAI 适配指南 - 在当前 7 Stage 工作流中引入 CrewAI 编排层
---

# CrewAI 使用指南

本文件说明如何把 CrewAI 接入当前仓库的 `ai-dev-workflow`，并保持现有 7 Stage 规则不变。

## 1. 定位

- CrewAI 负责“编排增强”：阶段诊断、下一步建议、风险摘要。
- `todolist.csv` 与 `process.md` 仍是唯一执行状态源。
- 默认入口 `scripts/ai/run-crewai.ps1` 会直接拉起 `EnterpriseDevFlow`，并通过本地执行 Agent（Codex/Gemini/Claude）完成 Stage 4 的真实代码实现。
- `ai-dev-workflow/crewai/` 中的 `labor-workflow-crew` 仅保留为遗留文本诊断模式。

## 2. 快速启动

在项目根目录执行：

```bash
powershell -ExecutionPolicy Bypass -File scripts/ai/run-crewai.ps1
```

默认输出文件：`docs/crewai/next-actions.md`

若只想运行遗留文本诊断模式：

```bash
powershell -ExecutionPolicy Bypass -File scripts/ai/run-crewai.ps1 -LegacyTextCrew
```

Flow 编排脚本（framework-plan 模式）：

```bash
python ai-dev-workflow/scripts/start.py --mode hybrid
python ai-dev-workflow/scripts/status.py --list
python ai-dev-workflow/scripts/resume.py <flow-id>
```

配置文件支持两种路径（二选一）：

1. `ai-dev-workflow/crewai/.env`
2. `ai-dev-workflow/config/.env`

## 3. 推荐触发时机

1. Stage 3 刚结束：让 CrewAI 生成 Stage 4 执行顺序建议。
2. Stage 4 中途卡住：让 CrewAI 做风险诊断和降级建议。
3. Stage 5 前：让 CrewAI 生成交接摘要草稿，供人工/Claude 审核。

## 4. 失败降级

若 CrewAI 无法运行或模型鉴权失败：

1. 在 `process.md` 记录 `crewai_unavailable:<reason>`。
2. 检查 `OPENAI_API_KEY` / `ANTHROPIC_API_KEY` / `GEMINI_API_KEY` 是否可用。
3. 立即回退到原生路由，不阻塞开发。

## 5. 相关文件

- `ai-dev-workflow/crewai/`
- `scripts/ai/run-crewai.ps1`
- `docs/crewai-integration.md`
