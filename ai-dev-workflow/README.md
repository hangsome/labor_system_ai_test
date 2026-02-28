# AI 大型系统开发工作流 (AI Dev Workflow)

> 版本: 1.2 | 创建时间: 2026-02-23 | 更新时间: 2026-02-26 | 适配: Codex / Claude / Antigravity

一套面向大型系统从零到一开发的 AI 工作流。通过分阶段执行、多 Agent 协作、CSV 任务跟踪，实现可追溯的全栈交付。

---

## 核心特性

- 7 Stage 流程：需求到发布运维全链路
- 多 Agent 协作：Codex / Gemini / Claude
- 任务级状态管理：`todolist.csv` + `process.md`
- 断点恢复：支持会话中断后继续执行
- 知识复用：`patterns/` 模式沉淀

## 前置条件

| 物料 | 说明 |
|------|------|
| Plan 文件 | 项目定位、模块、阶段、约束 |
| 需求文档 | PRD / 用户故事 / 功能列表 |
| 产品原型 | Figma / Axure / 手绘稿 |

---

## 目录结构

```text
ai-dev-workflow/
├── README.md
├── workflow-main.md
├── agent-routing.md
├── stages/                      # 7 个 Stage 定义
│   ├── stage-0-project-init.md
│   ├── stage-1-architecture.md
│   ├── stage-2-phase-planning.md
│   ├── stage-3-task-decomposition.md
│   ├── stage-4-execution.md
│   ├── stage-5-review-handoff.md
│   └── stage-6-deployment.md
├── templates/
│   ├── plan.md
│   ├── todolist.csv
│   ├── process.md
│   ├── feature-task.md
│   ├── test-strategy.md
│   ├── retrospective.md
│   └── pattern.md
└── adapters/
    ├── codex-prompt.md
    ├── claude-instructions.md
    └── antigravity-workflow.md
```

---

## 快速使用

### 在 Codex 中

```text
请按 ai-dev-workflow/workflow-main.md 中定义的工作流执行，开发 [项目名称]。
需求描述：[你的需求]
```

### 在 Claude 中

```text
请阅读 ai-dev-workflow/workflow-main.md 并严格按照其中的 7 阶段工作流执行。
项目需求：[你的需求]
```

### 在 Antigravity 中

参考 `adapters/antigravity-workflow.md`。

---

## 工作流概览

```text
Stage 0: 项目初始化
  -> Stage 1: 架构设计
  -> Stage 2: 阶段规划
  -> [Stage 3: 任务分解 -> Stage 4: 阶段执行 -> Stage 5: 审查交接] 循环
  -> Stage 6: 部署与运维就绪
  -> 项目交付
```

---

## Agent 协作

| Agent | 角色 | 触发条件 |
|-------|------|---------|
| Codex | 主编排 + 后端实现 | 默认 |
| Gemini | 前端实现 | `area=frontend` |
| Claude | 审计 + 评审 | 阻断门审查 |

详见 `agent-routing.md`。
