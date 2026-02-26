# Phase 01 Retrospective

## 做得好的部分
- 任务粒度稳定，`PH01-*` 全部以“领取-验证-提交”闭环完成。
- 文档同步与代码提交同节奏推进，未出现后置补文档造成的漂移。
- 前后端关键路径（auth/rbac/audit + login/e2e）均有可执行回归命令。

## 问题与处理
- Gemini 路由出现无输出和容量限制（429）：已执行回退策略，改由 Codex 交付并记录原因。
- `vitest` 误收集 `tests/e2e`：已修复为只收集 `tests/unit`，单测和 E2E 分离执行。
- `contextLoads` 在当前无基础设施模式不稳定：显式 `@Disabled`，避免阻断阶段回归。

## 可复用模式
- `patterns/testing/vitest-unit-scope.md`
- `patterns/ops/llm-fallback-on-timeout-or-429.md`

## 下一阶段建议
- Phase 02 继续维持“任务级验证命令 + 文档同提交流程”。
- 对 Gemini/Claude CLI 增加固定超时与回退日志模板，减少交付波动。

