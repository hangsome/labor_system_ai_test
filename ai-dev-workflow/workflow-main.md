---
description: AI 大型系统开发工作流 - 主编排文件（从零到一，分阶段执行）
version: "2.0"
updated_at: "2026-03-01"
compatible: codex, claude, antigravity, crewai
---

# AI 大型系统开发工作流 - 主编排

你现在进入「AI 大型系统开发工作流」模式。

本工作流面向大型系统从零到一的全栈开发，通过 7 个 Stage 的有序推进，将需求落地为可交付、可部署、可运维的系统。

> **启动方式**：用户提供 **Plan 文件**（见 `templates/plan.md`）或自然语言描述，AI 从 Stage 0 开始执行。

> **核心原则**：
> - 以内置 Plan 为主，外置 Skill 与全局提示词为辅
> - 按需调用 MCP（`sequential-thinking` + `context7` + `web-search`）
> - 多 Agent 协作：支持单 Agent 串行与多 Agent 并行（Orchestrator + Backend + Frontend + Audit 四角色独立会话）
> - 每阶段人工验收通过后再进入下一阶段
> - 结构化持久与断点续传：使用 `todolist.csv` + `process.md`
> - 安全回滚优先：默认定向恢复（`git restore`），谨慎使用破坏性命令

---

## 一、前置条件（开工前）

本工作流启动时，必须提供 Plan 文件或自然语言需求。辅助物料越充分越好。

| 物料 | 重要性 | 说明 |
|------|--------|------|
| Plan 文件 | 必须 | 系统定位、模块列表、阶段划分、技术偏好 |
| 产品原型图 | 强烈推荐 | Figma / Axure / 手绘稿 |
| 需求文档 | 强烈推荐 | PRD / 用户故事 / 功能列表 |
| Specification | 推荐 | 用户故事 + 验收标准 + NFR 矩阵 + 数据词典 |
| 竞品/参考系统 | 可选 | 提供参考链接，供调研 |

若只提供自然语言描述，AI 自动生成 Plan 并要求用户确认后继续。

---

## 二、总体行为约定（必须遵守）

1. **分阶段推进**：不跳跃 Stage，每个 Stage 有清晰输入/输出/完成条件。
2. **文件即状态**：计划、任务、进度必须落盘（CSV / Markdown）。
3. **Phase 局部状态优先**：执行态只写 `phases/phase-XX/process.md` 与 `phases/phase-XX/todolist.csv`，根目录 `process.md` 仅作为总览与交接摘要。
4. **开发期与迭代期分离**：开发期以单主 agent + 少量受控并行为主；迭代期才放宽到 ClawAI 批量调度。
5. **人工验收门**：Stage 5 是阻断门，AI 不得自行跳过。
6. **Agent 路由**：前端优先 Gemini，失败降级 Claude，再失败降级 Codex；后端默认 Codex；审计默认 Claude。
7. **KISS / YAGNI**：不引入无关复杂度。
8. **分支隔离**：每个 Phase 在 `feature/phase-XX-xxx` 分支执行，禁止直接在 `dev` 或 `prod` 开发。`dev` 为开发主干，`prod` 为生产主干，仅 Stage 6 发布时从 `dev` merge 到 `prod`。
9. **Stage 可重开策略**：Stage 0/1/2 默认一次性执行；若需求发生重大变更，可受控重开（需记录变更原因与影响）。
10. **知识复用**：每个 Phase 结束后提炼可复用模式写入 `patterns/`。
11. **Definition of Done（分层）**：
   - Stage 4 执行 DoD：`dev_state + test_state + git_state` 达标
   - Stage 5 验收 DoD：`review_state` 达标并通过人工验收
12. **演进同步原则**：Schema/API/架构文档与本地基础设施必须随每个 Phase 增量更新，禁止集中到项目末尾补写。
13. **产品对齐原则**：AI 在每个 Stage 开始时必须**主动扫描**项目目录，检测是否存在产品原型（`docs/prototypes/`、`design/`、`mockups/` 等目录中的图片/PDF/Figma 导出文件）或 UI 规格相关的需求文档（`docs/` 下的 PRD、需求文档等）。一旦发现，前端任务必须 **1:1 还原**原型设计，不得自行发挥或简化。对齐粒度包括：布局结构、组件层级、间距配色、字体排版、交互行为。Stage 4 执行时必须逐一对照原型，Stage 5 审查时必须进行视觉对齐度验收。
14. **前端交互闭环原则**：所有前端页面的路由跳转与交互操作必须形成**操作闭环**，禁止出现"导航死胡同"。具体要求：
    - 每个路由页面必须提供明确的**返回/回退入口**（返回按钮、面包屑导航或侧边栏激活态）
    - 用户跳转到任何页面后都必须能通过 UI 元素回到上级页面或主页，禁止出现只能依赖浏览器后退的页面
    - 模态弹窗 / 抽屉 / 多步表单必须有关闭或取消操作，确保用户可随时退出流程
    - 表单提交后必须有明确的结果反馈和后续导航路径（如返回列表、跳转详情页）
    - 错误页面（404/403/500）必须提供回到首页或上一页的导航入口
    - Stage 1 架构设计时须输出**导航闭环矩阵**，Stage 3 任务分解时须包含闭环验收项，Stage 4 执行时须校验闭环，Stage 5 审查时须进行全路由闭环审计
15. **多 Agent 协作原则**（多 Agent 并行模式下生效，详见 `multi-agent-protocol.md`）：
    - **目录隔离**：Backend Agent 仅操作 `src/backend/`，Frontend Agent 仅操作 `src/frontend/`，禁止交叉修改
    - **共享边界**：仅 `docs/api-contracts/` 为共享区域，由 Orchestrator 创建、Backend 更新 CHANGELOG、Frontend 读取
    - **合约驱动同步**：前后端通过 API 合约 (OpenAPI YAML) 同步接口定义，Breaking Change 必须通过 CHANGELOG 追踪
    - **分支隔离**：后端 `feature/phase-XX-<slug>`，前端 `feature/phase-XX-<slug>-fe`，均从 `dev` 切出，禁止在同一分支混合开发
    - **文件驱动协调**：Agent 间通过 `SYNC.md` + `process.md` + `todolist.csv` 进行状态同步，不依赖实时通信
    - **跨角色状态检查**：每个角色在任务批次完成后检查其他角色状态，提示用户启动未开始的角色

---

## 三、Stage 总览与流转

```
Stage 0 -> Stage 1 -> Stage 2 -> [Stage 3 -> Stage 4 -> Stage 5] -> 循环 -> Stage 6 -> 完成
```

| Stage | 名称 | 输入 | 输出 | 定义文件 |
|-------|------|------|------|---------|
| 0 | 项目初始化 | 用户需求描述 | 项目骨架 + 技术栈文档 + 规格文档 | `stages/stage-0-project-init.md` |
| 1 | 架构设计 | 项目骨架 + 需求 | 架构文档 + DB Schema + API 合约 + 测试策略 | `stages/stage-1-architecture.md` |
| 2 | 阶段规划 | 架构文档 | 阶段划分方案 + phase 目录结构 | `stages/stage-2-phase-planning.md` |
| 3 | 任务分解 | 当前 phase 范围 | `todolist.csv` + `process.md` + feature-tasks | `stages/stage-3-task-decomposition.md` |
| 4 | 阶段执行 | `todolist.csv` | 代码 + 测试 + 更新后的 CSV/process | `stages/stage-4-execution.md` |
| 5 | 审查交接 | 完成代码 + CSV | 审查报告 + 人工确认 + 下一阶段决策 | `stages/stage-5-review-handoff.md` |
| 6 | 部署与运维就绪 | 通过所有 Phase 的代码 | CI/CD + Docker + 部署文档 + 运维基线 | `stages/stage-6-deployment.md` |

---

## 四、执行流程摘要

### Stage 0：项目初始化
- 消化 Plan / 需求并生成 `docs/specification.md`
- 技术选型与项目脚手架
- **Git 分支初始化**：创建 `dev`（开发主干）+ `prod`（生产主干），执行 Branch Auto-Adapt Protocol 自动适配已有项目分支结构
- **Shift-Left 基线**：在 Stage 0 建立最小 CI 骨架（lint + unit + secrets-scan）

### Stage 1：架构设计
- 系统分解、模块边界、Schema、API
- 生成 `docs/test-strategy.md`
- Claude 架构评审通过后进入 Stage 2

### Stage 2：阶段规划
- 按依赖与业务优先级划分 3-6 个 Phase
- 生成 `docs/phase-plan.md` 与 `phases/*` 目录
- 明确每个 Phase 的文档刷新与基础设施演进责任

### Stage 3：任务分解
- 拆分原子任务并生成 `todolist.csv`
- 每条任务必须有 `acceptance_criteria` 和 `val_command`
- 强制补齐 migration/docs/infra 演进任务（如适用）
- CSV 保持轻量：长文本放 `feature-tasks/*.md`，CSV 仅保留摘要和执行入口

### Stage 4：阶段执行
- 按 CSV 执行任务
- 失败优先定向回滚，避免共享分支破坏
- 开发期默认关闭每批 review，改由批次末尾或 Stage 5 统一审查
- Stage 4 DoD：`dev/test/git` 达标；`review` 留给 Stage 5

### Stage 5：审查交接
- 代码清理、回归测试、Claude 审计
- 人工验收阻断门
- 校验文档同步与迁移一致性，发现漂移则打回修复
- 通过后回到 Stage 3 处理下一 Phase 或进入 Stage 6

### Stage 6：部署与运维就绪
- **合并发布**：将 `dev` merge 到 `prod`，打 Release Tag
- 在已有 CI 基线之上做生产化增强（镜像、环境、发布、回滚、监控）
- 校验各 Phase 的 migration 连续性与 schema 无漂移
- 输出最终部署文档与交付报告

---

## 五、断点续传与会话管理

### 5.1 中断恢复 (Context Recovery)

当检测到 `dev_state=进行中`：
1. 读取任务的 `started_at_commit`
2. 优先执行定向恢复：
   - `git diff --name-only <started_at_commit>..HEAD`
   - `git restore --source <started_at_commit> -- <changed-files>`
3. 仅在明确任务隔离分支且确认无协作改动时，才允许 `git reset --hard`
4. 恢复后继续执行下一条任务

### 5.2 上下文压缩切换阈值

- 在 `process.md` 维护 `compaction_count`
- 当 `compaction_count >= compaction_threshold`（默认 10）时，提交 checkpoint 并建议切换新会话

### 5.3 Crash Recovery

新会话输入“继续”后自动：
1. 扫描 `todolist.csv` + `process.md`
2. 恢复当前 Stage 与任务状态
3. 执行 5.1 恢复协议
4. `compaction_count` 重置为 0 并继续

---

## 六、商用化度量（建议纳入 process.md）

1. 交付效率：任务一次通过率、平均重试次数、阶段工时偏差
2. 质量指标：覆盖率趋势、回归失败率、阻塞任务占比
3. 运行指标：部署频率、变更失败率、恢复时间（可逐步对齐 DORA）

---

## 七、MCP 服务与工具建议

| MCP / Tool | 用途 | 使用时机 | 上下文控制 |
|------------|------|----------|-----------|
| `sequential-thinking` | 分解、架构、任务拆分 | Stage 0/1/2/3 | 低风险 |
| `context7` | 官方文档查询 | Stage 0/1/4 | 低风险 |
| `web-search` | 最佳实践与风险调研 | Stage 0/1/4 | 低风险 |
| `figma` | 产品原型设计数据提取（替代手动截图对比） | Stage 1/4/5 前端任务 | ⚠️ 高风险——见调用纪律 |
| `playwright` | 前端效果验证 + E2E 测试 + 导航闭环校验（替代 `browser`） | Stage 4/5 | ⚠️ 中风险——见调用纪律 |

**MCP 调用纪律（防上下文污染）**：

1. **按需调用**：仅在当前任务需要时调用对应 MCP，禁止"预加载"或"探索性"调用。
2. **结果落盘**：MCP 返回的大数据（Figma 设计规范 / 页面截图 / 测试报告）必须写入文件（如 `docs/prototypes/page-spec.md`、`.tmp/screenshots/`），后续从文件读取，禁止重复调用同一数据。
3. **粒度最小化**：请求最小范围的数据（单页面/单组件），禁止一次性 dump 全量设计文件或完整 DOM 快照。
4. **Figma 专项**：仅提取当前任务对应页面的布局结构、组件规范、颜色/间距数值，禁止拉取整个 Figma 项目的图层树。
5. **Playwright 专项**：截图保存到 `.tmp/screenshots/` 目录并以路径引用，禁止将 base64 截图或完整 DOM 内联到上下文中；E2E 测试结果仅返回 pass/fail + 失败截图路径。

---

## 八、引用

- `stages/stage-0-project-init.md`
- `stages/stage-1-architecture.md`
- `stages/stage-2-phase-planning.md`
- `stages/stage-3-task-decomposition.md`
- `stages/stage-4-execution.md`
- `stages/stage-5-review-handoff.md`
- `stages/stage-6-deployment.md`
- `templates/plan.md`
- `templates/todolist.csv`
- `templates/process.md`
- `multi-agent-protocol.md`
- `templates/sync.md`
- `templates/api-changelog.md`
