---
stage: 5
name: 审查交接
description: 阶段总结 + Claude 审计 + 人工验收 + 下一阶段转换
input: 已完成的代码 + CSV + process.md
output: 审查报告 + 验收确认
---

# Stage 5：审查交接

## 一、目标

对当前阶段完成的代码进行审计与回归验证，输出阶段总结报告并提交人工验收。

## 二、AI 行为约定

1. **不跳过人工验收**：AI 必须在提交审查报告后暂停，等待用户确认。
2. **Claude 必须参与阻断门审计**：发布前审计不可由自审替代。
3. **诚实报告**：如实描述完成情况，不掩盖 `validation_limited` 或阻塞项。
4. **阻断门原则**：若 Claude 不可用，Stage 5 必须挂起并转人工审查，禁止放行。

### 多 Agent 模式补充约定

当以审计工程师（Audit Engineer）角色进入时，以下规则生效：

5. **Boot Protocol 入口**：执行 `multi-agent-protocol.md` §3 的 Boot Protocol，自动检测待审计的 Phase。
6. **多分支审查**：需同时审查后端分支 (`feature/phase-XX-<slug>`) 和前端分支 (`feature/phase-XX-<slug>-fe`) 的代码。
7. **交接文档消费**：读取执行 Agent 生成的 `phases/phase-XX/handoff.md`，了解本 Phase 的变更概况。
8. **API 合约一致性审查**：验证后端实现与 `docs/api-contracts/` 中合约定义的一致性。
9. **审查完成后**：更新 `SYNC.md`，执行跨角色状态检查并提示用户下一步操作。

## 三、执行步骤

### Step 1：收集阶段数据

从 `todolist.csv` 与 `process.md` 汇总：

```
total_tasks: CSV 总行数
completed_tasks: dev_state=已完成 的行数
blocked_tasks: notes 包含 blocked: 的行数
validation_limited: notes 包含 validation_limited: 的行数
```

### Step 2：Garbage Collection (代码熵减)

在提交 Claude 审计前进行基线清理：
1. 清理无用代码（unused imports、注释残留、死代码）。
2. 运行 formatter/linter 并修复机械错误。
3. 校验 `docs/architecture.md` 与 API 合约一致性。
4. 校验本 Phase 涉及的文档已同步（`docs/api-contracts.md`、`docs/database-schema.md`、必要时 `docs/architecture.md`）。

### Step 2.5：回归测试 (Regression Testing)

1. 运行全量测试套件：
   - 后端全量单元测试 + 集成测试
   - 前端全量组件测试
2. 覆盖率对比：
   - 读取本 Phase 覆盖率
   - 对比上一 Phase 覆盖率基线
   - 覆盖率下降必须在报告中高亮告警
3. 将覆盖率数据写入 `process.md` 覆盖率追踪表。
4. 若涉及数据库结构变更，校验 migration 与 `docs/database-schema.md` 是否同提交链路一致；发现漂移必须阻断验收。

### Step 2.7：产品对齐度审查 (Product Alignment Review)

AI 必须主动扫描项目目录，检测是否存在产品原型文件（`docs/prototypes/`、`design/`、`mockups/` 等目录或 `docs/prototype-map.md`）。**一旦发现原型文件，必须执行以下视觉对齐度审查**：

1. **逐页面截图对比**：
   - 打开前端应用，遍历 `docs/prototype-map.md` 中所有页面
   - 使用 `playwright` MCP 对每个页面截图（保存到 `.tmp/screenshots/`）\n   - 若项目使用 Figma，可通过 `figma` MCP 直接拉取设计规范进行数值级对比
   - 将实际截图与原型图放在一起进行逐区域对比

2. **生成对齐度报告** `phases/phase-XX/review/alignment-report.md`：
   ```markdown
   # 产品对齐度报告

   ## 总体评级：✅ / ⚠️ / ❌

   | 页面 | 原型文件 | 对齐度 | 偏差说明 |
   |------|---------|--------|----------|
   | 登录页 | prototypes/login.png | ✅ 完全一致 | — |
   | 仪表盘 | prototypes/dashboard.png | ⚠️ 部分偏差 | 卡片间距偏大，图表配色不同 |
   | 用户管理 | prototypes/user-mgmt.png | ❌ 严重偏离 | 表格布局与原型不一致 |
   ```

3. **对齐度评级标准**：
   - ✅ **完全一致**：布局、组件、配色、间距均与原型一致
   - ⚠️ **部分偏差**：布局结构一致但细节有偏差（需列出具体偏差项）
   - ❌ **严重偏离**：布局或组件与原型有明显不同

4. **阻断规则**：
   - 存在任何 ❌ 项：**必须阻断验收**，追加修复任务后回到 Stage 4
   - 仅有 ⚠️ 项：记录偏差并由用户决定是否接受
   - 全部 ✅：对齐度审查通过

### Step 2.8：导航闭环审查 (Navigation Closed-Loop Audit)

AI 必须在交付审查时对前端应用进行全局导航闭环审查：

1. **全路由遍历**：
   - 打开前端应用，遍历所有已实现的路由页面
   - 对每个页面检查：是否有返回/回退入口、面包屑或侧边栏激活态

2. **死胡同检测**：
   - 检查每个页面是否能通过 UI 操作回到上级页面或首页
   - 检查错误页面（404/403/500）是否提供回到首页的入口
   - 检查表单提交后是否有明确的结果反馈和导航路径

3. **生成导航闭环报告** `phases/phase-XX/review/nav-audit-report.md`：
   ```markdown
   # 导航闭环审查报告

   ## 总体评级：✅ / ⚠️ / ❌

   | 页面/路由 | 返回入口 | 面包屑 | 死胡同 | 备注 |
   |----------|---------|--------|--------|------|
   | /dashboard | ✅ 侧边栏 | ✅ | 无 | — |
   | /users/:id | ✅ 返回按钮 | ✅ | 无 | — |
   | /404 | ❌ 无入口 | — | 是 | 需添加回首页按钮 |
   ```

4. **阻断规则**：
   - 存在任何导航死胡同（❌）：**必须阻断验收**，追加修复任务后回到 Stage 4
   - 全部 ✅：导航闭环审查通过

### Step 3：Claude 代码审计（阻断门）

审计要点：
- 安全性：注入漏洞、认证授权、敏感信息、输入校验
- 代码质量：可读性、错误处理、性能隐患、风格一致性
- 架构一致性：是否遵循 Stage 1 设计、API/Schema 是否一致

> 若 Claude 不可用：记录 `claude_unavailable:blocked`，通知用户并挂起 Stage 5。

### Step 4：生成审查报告

文件路径：`phases/phase-XX/review/review-summary.md`

报告至少包含：
1. 基本信息（阶段、时间、审查 Agent）
2. 完成概况（总任务、已完成、阻塞、受限验收）
3. 风险分级（严重/中等/建议）
4. 受限验收项与手动验证步骤
5. 文档同步状态（API/Schema/Architecture 是否与代码一致）
6. 迁移一致性状态（是否存在 schema drift）
7. **产品对齐度状态**（若有原型：引用 `alignment-report.md` 结论，列出偏差项）
8. **导航闭环状态**（引用 `nav-audit-report.md` 结论，列出导航死胡同项）

### Step 4.5：经验提炼与复盘 (Knowledge Extraction)

每个 Phase 完成后，提炼可复用经验：
1. 生成 `phases/phase-XX/review/retrospective.md`
2. 从 `notes` 中提取 `blocked:` / `failed:` 根因
3. 识别可复用模式并写入 `patterns/<category>/<slug>.md`

Pattern 分类：
- `patterns/architecture/`
- `patterns/code/`
- `patterns/config/`
- `patterns/testing/`
- `patterns/security/`
- `patterns/gotchas/`

### Step 5：提交人工验收（阻断门）

输出验收交接：

```
📋 Phase XX 验收交接

✅ 完成：XX/XX 任务
⚠️ 阻塞：XX 任务（详见审查报告）
⚠️ 受限验收：XX 任务（需手动测试）

📎 审查报告：phases/phase-XX/review/review-summary.md
📎 任务清单：phases/phase-XX/todolist.csv
📎 进度记录：phases/phase-XX/process.md
```

AI 在此必须暂停，等待用户反馈。

### Step 6：处理用户反馈

**反馈 A：✅ 验收通过**
1. 在 `process.md` 记录阶段完成与验收时间。
2. 合并 `feature/phase-XX-<slug>` 到 `main`（`--no-ff`）并打 Release Tag。
3. 若有下一 Phase，回到 Stage 3；否则进入 Stage 6。

**反馈 B：❌ 验收不通过，需要修复**
1. 记录问题。
2. 将相关任务状态回退为 `进行中`。
3. 若是“新代码破坏历史功能”且当前 `todolist.csv` 无对应任务，则在当前 Phase 动态追加一条回归缺陷任务：
   - `id=BUG-<phase>-<seq>`（例如 `BUG-PH02-001`）
   - `area` 按归属填 `backend/frontend/both`
   - `task_type` 按归属填 `backend/frontend/test-integration/test-e2e`
   - `dependencies` 指向引入回归的任务 ID
   - `notes` 记录 `regression_from:<task-id>`
4. 若问题属于文档/迁移漂移（代码已变更但 docs 或 migration 缺失），必须追加 `docs`/`database` 修复任务后再回到 Stage 4。
5. 回到 Stage 4 修复，修复后重新进入 Stage 5。

**反馈 C：📝 需求变更**
1. 更新 `todolist.csv`（新增/修改任务）。
2. 回到 Stage 4 执行新增任务。
3. 完成后重新进入 Stage 5。

## 四、阶段转换逻辑

```
Stage 5 结束
  if (还有未执行的 Phase):
    -> Stage 3
  else:
    -> Stage 6（部署与运维就绪）
```

## 五、完成条件

- [ ] Claude 审计已完成（或已明确转人工阻断）
- [ ] 审查报告已生成
- [ ] （若有原型）产品对齐度审查已完成，无 ❌ 项
- [ ] 导航闭环审查已完成，无导航死胡同项
- [ ] 用户已确认验收结果
- [ ] 阶段状态已更新
- [ ] 已确定下一步行动（下一阶段 / Stage 6 / 项目完成）

## 六、输出

- `phases/phase-XX/review/review-summary.md`
- `phases/phase-XX/review/retrospective.md`（可选，推荐）
- 更新后的 `process.md`（含验收结果）
- 下一阶段启动信号或进入 Stage 6
