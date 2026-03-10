# AI 协作开发工作手册

## 1. 目标

这份手册定义当前项目最适合的 AI 协作方式，目标是保住已完成的 Phase 02 基线，同时让 Phase 03 及后续阶段的 AI 试验不再污染主开发流。

## 2. 当前推荐模式

### 2.1 开发期模式

适用于 Phase 开发、跨层联动、数据库到前后端串行落地。

- 主执行器：Codex 单会话
- 辅助执行器：1 到 2 个手动 CLI agent
- ClawAI 角色：只处理低耦合、依赖已满足、写入范围清晰的 ready 队列
- 状态源：只写 `phases/phase-XX/process.md` 和 `phases/phase-XX/todolist.csv`
- 运行建议：`scripts/ai/run-crewai.ps1 -Profile development -TargetPhase <N>`

### 2.2 迭代期模式

适用于上线后的小功能、缺陷修复、文档同步和批量机械性改动。

- 主执行器：Codex 或单个 CLI agent
- ClawAI 角色：批量处理脚手架、验证脚本、低风险文档刷新
- 可接受降级：`codex CLI -> claude CLI -> codex API`
- 运行建议：`scripts/ai/run-crewai.ps1 -Profile maintenance -TargetPhase <N>`

## 3. 任务分流规则

### 3.1 直接用 Codex 单会话

- 涉及数据库 migration、后端接口、前端页面联动的一整条链路
- 依赖关系强，必须连续保持上下文的任务
- 需要边跑边修测试、边改边排障的任务

### 3.2 手动并行 CLI agent

- 写入范围可以明确切开
- 可提前定义 owner 文件夹
- 不依赖另一个 agent 先改完才能开始

推荐拆法：

- Agent A：数据库 / migration / schema docs
- Agent B：后端 service / controller / tests
- Agent C：前端页面 / API client / route / unit tests

### 3.3 用 ClawAI 编排

适合：

- 阶段内 ready 队列已经准备好
- 批量补 `scripts/validation/*.ps1`
- 批量补 docs、feature-task、phase 过程留痕
- 夜间自动续跑低风险任务

不适合：

- 需求还在变化的大功能
- 强依赖链任务
- 需要长上下文的复杂实现
- 当前结构尚未稳定的重构

## 4. 仓库治理规则

- 根目录 `process.md` 视为总览，不作为执行期状态源
- Phase 执行只写 `phases/phase-XX/`
- `.crewai/flows/` 与 `docs/crewai/` 视为运行时产物，不进入稳定开发分支
- Phase 试验统一放在独立 debug 分支
- 正式开发分支只合并经过验证的工作流修复，不合并试验期业务残片

## 5. ClawAI 优化点

- 开发期默认禁用 execution API fallback，避免“写不进文件却消耗 token”
- Stage 4 review 改成按频率触发，不再每批必跑
- 任务分解必须绑定真实仓库路径，禁止生成占位目录
- 执行入口强制要求 `-TargetPhase`，防止误扫整个仓库
- 在提交前运行 `scripts/ai/check-duplicate-artifacts.ps1 -FailOnDuplicate`

## 6. 推荐操作顺序

1. 先用 Codex 完成 Phase 目标和边界确认。
2. 把当前 Phase ready 任务拆成可写边界清晰的批次。
3. 核心链路用 Codex 单会话完成。
4. 低耦合批次再交给 ClawAI。
5. 每个批次结束后只更新 phase 局部状态，不回写根目录总览。
