---
description: Antigravity 适配指南 — 在 Antigravity 中正确运行 AI Dev Workflow v1.2（7 Stage）
---

# Antigravity 使用指南

适配版本：
- Workflow: `v1.2`
- Stage: `7 Stage (0-6)`

## 1. 快速接入

在项目根创建：`.agents/workflows/dev-workflow.md`。

建议写入以下最小模板：

```markdown
---
description: AI Dev Workflow v1.2 (7 Stage)
---

1. 先读取 `ai-dev-workflow/workflow-main.md`
2. 按顺序执行：
   Stage 0 -> Stage 1 -> Stage 2 -> [Stage 3 -> Stage 4 -> Stage 5] 循环 -> Stage 6
3. 每个 Stage 开始前读取对应 `ai-dev-workflow/stages/stage-X-*.md`
4. 状态文件：`phases/phase-XX/todolist.csv` + `phases/phase-XX/process.md`
5. 用户输入“继续”时执行中断恢复
```

## 2. Antigravity 中的角色映射

| 工作流角色 | Antigravity 对应 |
|---|---|
| Codex（编排/后端） | Antigravity 主执行体 |
| Gemini（前端） | 可选：调用本地 `gemini` CLI；不可用时 Antigravity 兜底 |
| Claude（审计） | 可选：外部审计输入；无外部审计时必须人工复核 |

## 3. 工具映射建议

| 工作流动作 | Antigravity 常用工具 |
|---|---|
| 规划分解 | `mcp_sequential-thinking_sequentialthinking` |
| 读写工作流文件 | `read_file` / `write_to_file` / `replace_file_content` |
| 执行测试与命令 | `run_command` |
| 搜索定位 | `find_by_name` / `grep_search` |
| 前端验证 | `browser_subagent` |
| 人工验收通知 | `notify_user` |

## 4. 正确执行要点

1. 不跳 Stage，必须包含 Stage 6。
2. Stage 4 与 Stage 5 DoD 分层执行：
   - Stage 4：`dev/test/git`
   - Stage 5：`review + 人工验收`
3. 多 Phase 演进必须同步落盘：
   - DB 结构变更 -> migration + `docs/database-schema.md`
   - API/架构变更 -> `docs/api-contracts.md` / `docs/architecture.md`
   - 新中间件 -> 先更新并验证 `docker-compose.dev.yml`
4. 回滚优先定向恢复；`git clean` 仅允许路径限定差集清理。

## 5. 启动模板

```text
请按 ai-dev-workflow/workflow-main.md 与 adapters/antigravity-workflow.md 执行 7 Stage。
需求：<你的项目需求>
```

## 6. 常见误用

1. 仍按旧版 6 Stage 执行。
2. 跳过 Stage 5 人工验收。
3. 不维护 `todolist.csv` / `process.md` 状态。
4. 把 migration 与文档同步留到最后统一补。

## 7. 参考

- `ai-dev-workflow/workflow-main.md`
- `ai-dev-workflow/stages/stage-3-task-decomposition.md`
- `ai-dev-workflow/stages/stage-4-execution.md`
- `ai-dev-workflow/stages/stage-5-review-handoff.md`
- `ai-dev-workflow/stages/stage-6-deployment.md`
