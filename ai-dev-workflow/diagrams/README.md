# AI Dev Workflow — 图表索引

> 所有图表均为 Mermaid 格式 (`.mmd`)，可在 VSCode (Mermaid Preview 插件)、GitHub、Notion 等支持 Mermaid 的平台中渲染。

## 图表清单

| # | 文件 | 内容 | 适用场景 |
|---|------|------|---------|
| 1 | `workflow-overview.mmd` | **7 Stage 主流转图** — 从需求到交付的完整阶段流转，含 Phase 循环和阻断门 | 整体理解工作流骨架 |
| 2 | `agent-architecture.mmd` | **多 Agent 角色架构图** — 4 角色分工、文件系统消息总线、Git 分支隔离 | 理解 Agent 协作模式 |
| 3 | `degradation-chain.mmd` | **降级链与异常处理** — 前端降级链、Claude 不可用处理、管道快捷审查 | 异常场景处理参考 |
| 4 | `parallel-timeline.mmd` | **并行窗口时间线** — 后端领先前端 1 Phase 的 Gantt 图 | 多 Agent 并行排期 |
| 5 | `phase-execution-detail.mmd` | **单 Phase 执行子流程** — Stage 3→4→5 的详细步骤、路由决策、验证循环 | 执行阶段详细参考 |
| 6 | `boot-protocol.mmd` | **会话启动协议** — 6 步 Boot Protocol 流程图 | 新会话初始化参考 |
