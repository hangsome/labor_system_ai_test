---
stage: 4
name: 阶段执行
description: AI 按 todolist.csv 逐条执行任务，多 Agent 协作编码 + 测试
input: todolist.csv + process.md
output: 代码 + 测试 + 更新后的 CSV/process
---

# Stage 4：阶段执行

## 一、目标

按 todolist.csv 中定义的任务，逐条执行编码和测试工作，多 Agent 协作，实时更新进度，直到当前阶段所有任务完成。

## 二、AI 行为约定

1. **CSV 是边界与状态源**：只做 CSV 当前行描述的工作；任何范围变更先更新 CSV，再写代码。
2. **原子化提交与失败回滚 (Transaction Execution)**：任务代码通过 `val_command` 机械校验后立即原子化提交。失败时优先使用**非破坏性回滚**（`git restore` 定向恢复任务相关文件）。
3. **上下文窗口恢复 (Context Recovery)**：遇到中断重启时，若任务处于 `进行中`，必须基于 `started_at_commit` 清理任务残留后重试。
4. **Agent 路由强制执行**：
   - `assigned_agent=gemini`：调用本地 Gemini CLI
   - `assigned_agent=codex`：由当前 Agent 直接执行
   - `assigned_agent=claude`：调用 Claude 子代理
   - **前端降级链**：Gemini → Claude → Codex。Gemini 返回 429/限流/失败时降级 Claude；Claude 也不可用时最终降级 Codex。降级时在 `notes` 记录 `agent_fallback:<原agent>→<降级agent>:<原因>`。
5. **闭环不可缺省**：每条任务必须完成“编码 -> 机械校验 -> 提交 -> 状态更新”。
6. **状态驱动**：仅使用枚举值更新状态字段。
7. **KISS / YAGNI**：不做无关重构，不引入新架构，优先修根因。
8. **安全边界**：
   - 禁止在共享分支（`main` / `feature/*`）使用全局 `git reset --hard`、`git clean -fd`。
   - 如需清理未跟踪文件，必须基于 before/after 差集并使用**路径限定**命令：`git clean -fd -- <paths>`。

### 多 Agent 并行模式补充约定

当以角色身份（Backend / Frontend / Audit Engineer）进入时，以下规则生效：

9. **角色作用域执行**：仅执行 CSV 中 `area` 或 `role` 匹配本角色的任务，不触碰其他角色的任务。
10. **分支自动检测**（Boot Protocol Step 4）：
    - 会话启动时执行 `git branch --show-current` 检测当前分支
    - 后端期望分支：`feature/phase-XX-<slug>`，前端期望分支：`feature/phase-XX-<slug>-fe`
    - 若不匹配则自动 checkout 或 create，前端分支从后端已合并的 `main` 切出
    - 对任意项目自动适配：扫描目录结构检测前后端分离特征
11. **目录隔离**：Backend 仅操作 `backend/`，Frontend 仅操作 `frontend/`，禁止交叉修改。
12. **API 合约监控**（仅 Frontend 角色）：
    - 每轮任务前检查 `docs/api-contracts/CHANGELOG.md`
    - 若发现 Breaking Change 且当前任务的 `contract_version` 与最新版本不一致，自动创建 REWORK 任务并标记 `notes=contract_rework:v<old>→v<new>`
    - 前端可基于合约 YAML Mock 数据开发，后端就绪后切换 `baseURL`
13. **会话结束协议**：任务批次完成后必须：
    - 更新 `SYNC.md`（使用 `templates/sync.md` 格式）
    - 执行跨角色状态检查（见 `multi-agent-protocol.md` §8）
    - 若其他角色有未开始任务，提示用户启动对应角色

## 三、执行流程

### 整体循环 (Infinite Loop Protocol)

```
读取表 -> 选择下一任务 -> [记录基准 commit] -> 执行编码
[执行 val_command 校验]
  ├─(成功)-> git commit -> 更新 CSV 为完成
  └─(失败)-> 定向回滚 -> attempts+1 -> 更新 CSV 为进行中/失败
-> 循环直至全部完成
```

### Step 1：读取 CSV + 上下文窗口恢复

```powershell
# 读取 CSV
$tasks = Import-Csv "phases/phase-XX/todolist.csv"
```

**Context Window Recovery 断点恢复**：
检查 `dev_state=进行中` 的任务，代表上一轮会话意外中断。
- 如果存在此类任务，检查其 `started_at_commit`。
- 使用定向恢复（推荐）：
  1. `git diff --name-only <started_at_commit>..HEAD` 获取改动文件列表。
  2. `git restore --source <started_at_commit> -- <changed-files>` 恢复任务相关文件。
  3. 对比未跟踪文件基线，仅清理本任务新增的 untracked 文件：
     ```bash
     # 若 .tmp/untracked-before-<task-id>.txt 缺失，则跳过清理并记录告警
     git ls-files --others --exclude-standard > .tmp/untracked-after-<task-id>.txt
     # 计算差集：new_untracked = after - before
     git clean -fd -- <new-untracked-files>
     ```
- 仅当处于明确任务隔离分支、且确认不会影响他人改动时，才允许 `git reset --hard <started_at_commit>`。
- 将任务 `attempts` 增加 1，然后重新置入执行队列。

### Step 2：选择下一条任务

**选择规则**（优先度）：
1. 优先收敛半成品（恢复中断任务）。
2. 选择依赖满足且 `dev_state != 已完成` 且 `attempts < 3` 的任务。
3. 若前置依赖有最终失败任务（`attempts>=3`），当前任务标记 `failed_blocked` 并跳过。
4. 优先级排序：P0 > P1 > P2。

### Step 3：事务执行循环 (Transaction Execution Cycle)

#### 3.1 声明并锁定 (Claim)
- 获取当前 Git Hash：`git rev-parse HEAD`
- 写入 CSV：`started_at_commit` = 当前 Hash
- `dev_state` -> `进行中`
- `notes` 追加 `started_at:<ISO8601>`
- 记录未跟踪文件基线（供失败回滚差集清理使用）：
  ```bash
  mkdir -p .tmp
  git ls-files --others --exclude-standard > .tmp/untracked-before-<task-id>.txt
  ```

#### 3.2 编码执行 (Execute)
- 参考 `acceptance_criteria` 摘要驱动编码；若 `refs` 指向 `feature-tasks/*.md`，以该文件中的详细验收标准为准。
- `assigned_agent=gemini`：调用 Gemini CLI 并注入 `val_command`。若 Gemini 返回 429/限流/失败，立即降级为 Claude；若 Claude 也不可用，最终降级为 Codex。
- `assigned_agent=claude`：调用 Claude 子代理。
- `assigned_agent=codex`：当前 Agent 直接修改文件。

**原型对照协议（AI 自动扫描触发，前端任务必须执行）**：

> 每次执行前端任务前，AI 必须主动检查 `docs/prototype-map.md` 是否存在。若不存在，则扫描项目目录（`docs/prototypes/`、`design/`、`mockups/` 等）查找原型文件。一旦发现原型文件，必须先补建 `docs/prototype-map.md`，然后严格按照原型 1:1 还原。

1. **编码前**：读取 `refs` 中引用的原型文件（或从 `docs/prototype-map.md` 找到对应原型），逐像素级理解原型的布局结构、组件层级、配色方案、字体排版、间距尺寸、交互行为。
2. **编码中**：严格按原型还原，不得自行发挥或简化设计。重点关注：
   - 布局结构（Flex/Grid、各区域占比）
   - 组件选型（按钮样式、表单元素、卡片组件等）
   - 视觉细节（边距、圆角、阴影、颜色值）
   - 交互行为（悬停效果、动画、加载状态）
3. **编码后**：使用 `playwright` MCP 打开前端页面并截图（保存到 `.tmp/screenshots/`），与原型进行逐区域对比，确认视觉一致性。若项目使用 Figma，可通过 `figma` MCP 直接拉取设计规范（间距/颜色/字号）对比。

#### 3.3 机械校验 (Validate)
- 运行任务 `val_command`（可为测试命令或校验脚本入口）；若无则必须运行最相关测试。
- 退出码 0 -> PASS。
- 非 0 或超时 -> FAIL。
- 环境依赖缺失同样视为 FAIL，不得通过绕过测试来“伪通过”。

#### 3.3.0 视觉对齐校验 (Visual Alignment Check)
当任务为前端任务且 `docs/prototype-map.md` 存在时，在机械校验后额外执行：
1. 使用 `playwright` MCP 打开对应前端页面并截图（保存到 `.tmp/screenshots/`）。
2. 将截图与 `refs` 中引用的原型文件进行逐区域对比：
   - 布局结构是否一致
   - 组件类型与位置是否一致
   - 配色、字体、间距是否接近
3. 对比结果记录到 `notes` 字段：`visual_alignment:<pass/partial/fail>`
   - `pass`：布局、组件、样式均与原型一致
   - `partial`：布局结构一致但细节有偏差（如颜色不完全匹配），需记录具体偏差项
   - `fail`：布局或组件有明显偏离，任务视为未通过，必须修复后重新校验
4. `visual_alignment=fail` 的任务等同于 `val_command` 失败，进入失败路径处理。

#### 3.3.1 导航闭环校验 (Navigation Closed-Loop Check)
当任务为前端任务且涉及路由/页面/弹窗时，在机械校验后额外执行：
1. 使用 `playwright` MCP 导航到该页面，验证以下闭环要素：
   - 页面是否有明确的返回/回退入口（返回按钮、面包屑或侧边栏）
   - 从该页面能否通过 UI 操作回到上级页面或首页
   - 模态弹窗/抽屉/表单是否有关闭/取消操作
   - 表单提交后是否有结果反馈和后续导航路径
2. 对比结果记录到 `notes` 字段：`nav_closed_loop:<pass/fail>`
   - `pass`：所有导航路径均可闭环
   - `fail`：存在导航死胡同，必须修复后重新校验
3. `nav_closed_loop=fail` 的任务等同于 `val_command` 失败，进入失败路径处理。

#### 3.3.2 覆盖率门禁 (Coverage Gate)
当任务类型为 `test-unit` 或 `test-integration` 时：
1. 解析覆盖率输出（控制台或 `coverage/` 报告）。
2. 与 `docs/test-strategy.md` 的覆盖率目标对比。
3. 低于目标：`notes` 记录 `coverage:<actual>/<target>`，并在 Stage 5 重点审查。
4. 达标：`notes` 记录 `coverage:<actual>` 作为基线。

#### 3.4 结算 (Record Outcome)

**成功路径 (PASS)**：
1. 更新 CSV：`dev_state=已完成`、`test_state=已完成`、`git_state=已提交`、`notes` 追加 `done_at:<ISO8601>`。
2. `review_state` 由 Stage 5 审计与验收阶段统一更新，Stage 4 不将其置为 `已完成`。
3. 原子化提交：
   ```bash
   git add <changed-files> phases/phase-XX/todolist.csv phases/phase-XX/process.md
   git commit -m "[<task-id>] <title>"
   ```

**失败路径 (FAIL)**：
1. `attempts + 1`，`notes` 记录失败原因。
2. 若存在 `started_at_commit`，执行定向回滚：
   ```bash
   git diff --name-only <started_at_commit>..HEAD
   # 按输出文件列表恢复改动，避免误伤无关文件
   git restore --source <started_at_commit> -- <changed-files>
   # 比较 untracked 基线，仅清理本任务新增文件（差集为空则跳过）
   # 若 before 基线缺失，则禁止执行 clean，只记录告警
   git ls-files --others --exclude-standard > .tmp/untracked-after-<task-id>.txt
   git clean -fd -- <new-untracked-files>
   ```
3. 仅在确认任务隔离分支且无协作改动时，才允许 `git reset --hard <started_at_commit>` 作为最后手段。
4. `attempts >= 3` 则 `dev_state=失败`；否则留在队列下轮重试。

### Step 4：更新 process.md

将任务结算结果同步到 `process.md` 的“已完成/阻塞”列表与统计数据。

### Step 5：循环进入下一任务或触发会话切换

每完成一条任务后检查 `process.md` 中的 `compaction_count`：
- `compaction_count < compaction_threshold`（默认 10）-> 继续下一任务
- `compaction_count >= compaction_threshold` -> 触发会话切换，提交 checkpoint 并通知用户

## 四、阻塞处理（Fail-Safe）

当某条任务无法在 3 次 attempt 解决，或缺失外部资源：
1. 标记 `dev_state=失败`，`notes` 记录 `blocked:<原因>`。
2. 在 `process.md` 阻塞表中记录。
3. 继续执行不受影响任务。
4. 若剩余任务全部被阻塞树牵连，则主动报告并停止。

## 五、完成条件

- [ ] CSV 中所有任务满足 Stage 4 DoD：`dev_state=已完成` + `test_state=已完成` + `git_state=已提交`
- [ ] `review_state` 允许保持 `未开始/进行中`，待 Stage 5 审计通过后再置为 `已完成`
- [ ] 所有测试通过（或 `validation_limited` 且已记录并经人工确认）
- [ ] 所有代码已 Git 提交且可追溯到任务 ID
- [ ] `process.md` 已更新至最新状态
- [ ] 无 P0 阻塞项

## 六、输出到下一 Stage

- 已完成的代码
- 更新后的 `todolist.csv`
- 更新后的 `process.md`
- Git 提交历史
- （多 Agent 模式）更新后的 `SYNC.md`
- （多 Agent 模式）跨角色状态检查报告

