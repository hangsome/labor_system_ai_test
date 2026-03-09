---
stage: 3
name: 任务分解
description: 将当前阶段的功能细化为原子任务，生成 todolist.csv、process.md 和 feature-task 文件
input: 当前 phase 的范围定义
output: todolist.csv + process.md + feature-tasks/
---

# Stage 3：任务分解

## 一、目标

将当前开发阶段的功能需求分解为原子级任务，生成可执行的 todolist.csv，同时创建 process.md 和 feature-task 文件，为 Stage 4 的执行提供完整的任务清单。

## 二、AI 行为约定

1. **每个任务必须是原子级的**：一个任务 = 一个可独立完成、可独立测试的工作单元。
2. **必须覆盖前端 + 后端 + 测试**：不遗漏任何维度。
3. **CSV 是唯一状态源**：所有任务状态通过 CSV 管理。
4. **使用 `sequential-thinking`**（totalThoughts: 6-8）辅助任务拆分。
5. **CSV 编码必须是 UTF-8 with BOM**（兼容 Excel）。
6. **数据库变更必须迁移先行**：凡涉及表结构变更，必须在同一 Phase 内生成版本化 migration 任务与 schema 文档更新任务。
7. **文档必须随代码演进**：凡涉及 API/领域模型/架构边界变更，必须生成对应 docs 刷新任务，禁止“最后统一补文档”。
8. **CSV 保持轻量路由**：长文本验收标准与复杂验证步骤写入 `feature-tasks/*.md`，CSV 仅保留摘要与可执行入口。
9. **中间件变更必须前置基础设施任务**：凡引入 Redis/MQ/ES 等依赖，先更新 `docker-compose.dev.yml` 并验证启动，再执行业务代码任务。
10. **任务路径必须绑定真实仓库结构**：生成 `refs` 与目标目录前，必须先扫描当前仓库的真实前后端根路径；禁止把任务指向仓库中不存在的占位路径（如未实际存在的 `backend/src/...`、`frontend/src/views/...`）。

## 三、任务拆分规则

### 拆分粒度

一个功能点通常拆分为以下任务：

```
功能点: 用户登录
├── DB   → 创建 user 表 DDL
├── BE   → 用户注册 API (POST /api/v1/auth/register)
├── BE   → 用户登录 API (POST /api/v1/auth/login)
├── BE   → JWT Token 签发与验证
├── BE   → 用户注册/登录单元测试
├── FE   → 登录页面组件
├── FE   → 注册页面组件
├── FE   → 登录/注册表单校验
├── FE   → 路由守卫（未登录跳转）
├── FE   → 前端组件测试
├── INT  → 登录注册集成测试
└── REV  → 代码审查
```

### 任务类型 (task_type)

| 类型 | 说明 | 典型 Agent |
|------|------|-----------|
| `database` | 数据库 DDL、迁移脚本 | Codex |
| `backend` | 后端 API、Service、Repository | Codex |
| `frontend` | 前端页面、组件、样式 | Gemini |
| `api` | API 集成、接口对接 | Codex |
| `test-unit` | 单元测试 | 同代码 Agent |
| `test-integration` | 集成测试 | Codex |
| `test-e2e` | 端到端测试 | Codex |
| `review` | 代码审查 | Claude |
| `config` | 配置文件、环境变量 | Codex |
| `docs` | 文档更新 | Codex |

### Agent 分配规则

| area | task_type | assigned_agent |
|------|-----------|---------------|
| backend | * | codex |
| frontend | * | gemini |
| both | database | codex |
| both | test-integration | codex |
| both | test-e2e | codex |
| * | review | claude |

> Gemini 不可用时（429 等），frontend 任务降级为 Claude；若 Claude 也不可用，最终降级为 Codex。

### 测试任务生成规则

每个功能点的测试任务必须遵循以下最低标准：

| 条件 | 必须生成的测试任务 |
|------|------------------|
| 后端 Service/Repository 方法 | 至少 1 条 `test-unit` 任务 |
| API 端点 | 至少 1 条 `test-integration` 任务 |
| 前端交互组件 | 至少 1 条 `test-unit`（组件测试）任务 |
| 核心用户故事 (US-xxx) | 至少 1 条 `test-e2e` 任务 |

### val_command 规范

每条 CSV 任务的 `val_command` 必须符合以下规范：

| 任务类型 | val_command 要求 | 示例 |
|---------|-----------------|------|
| `backend` | 包含相关单元测试 | `mvn test -Dtest=UserServiceTest` |
| `frontend` | 包含组件测试 | `npm test -- --testPathPattern=LoginForm` |
| `database` | 验证 DDL 可执行 | `mysql -u root < migration.sql && echo OK` |
| `test-unit` | 显示覆盖率 | `npm test -- --coverage --testPathPattern=auth` |
| `test-integration` | 启动测试服务 | `mvn verify -Dtest=UserApiIntegrationTest` |
| `test-e2e` | 运行 E2E 框架 | `npx playwright test tests/login.spec.ts` |

> ⚠️ **禁止 val_command 为空**。无法定义机械校验的任务，必须在 `notes` 中写 `val_manual:<手动验证步骤>` 并将 `test_state` 标记为 `manual_required`。
> 阶段验收前，`manual_required` 任务必须补齐人工验证证据，并将 `test_state` 回填为 `已完成`（或由用户明确接受 `validation_limited`）。
> 🔒 **安全断言**：`val_command` 的副作用必须限制在当前项目工作区与本地开发依赖（如 `docker-compose.dev.yml`）内，禁止破坏性系统命令（如 `rm -rf`、`del /f /s /q`、`git reset --hard`）以及任何直连生产环境命令（生产 DB/线上 SSH/线上 API 写操作）。
> ✅ **推荐验证命令族**：`npm/pnpm/yarn`、`mvn/gradle`、`pytest`、`go test`、`dotnet test`、`playwright`、`curl localhost`。
> 🧩 **CSV 抗脆弱约束**：`val_command` 必须是单行可执行命令。复杂命令请落盘为脚本（如 `scripts/validation/<task-id>.ps1` / `.sh`），CSV 中仅引用脚本入口命令。

## 四、执行步骤

### Step 0：创建阶段特性分支 (Phase Feature Branch)

在做任何任务分解之前，**必须先为当前 Phase 创建独立的特性分支**，将后续所有开发工作隔离在该分支上：

```bash
# 确保在 dev 分支上且状态干净
git checkout dev
git pull origin dev  # 如有远程仓库

# 创建并切换到阶段特性分支
git checkout -b feature/phase-XX-<slug>
# 示例: git checkout -b feature/phase-01-foundation
```

> **⚠️ 重要**：Stage 4 的所有原子化提交 (Atomic Commits) 均发生在此特性分支上。禁止在 `main` 上直接开发。

### Step 1：读取阶段范围

从 `phases/phase-XX/plan.md` 或 `docs/phase-plan.md` 中提取当前阶段范围：
- 包含哪些模块/功能
- 需要创建/修改哪些表
- 需要创建哪些 API
- 需要创建哪些页面
- 是否引入新中间件（Redis/MQ/ES/对象存储等）
- 是否发生架构边界或领域模型变更（需同步文档）

同时必须扫描仓库结构并记录真实落盘根目录：
- 后端代码根路径（示例：`backend/continew-extension/...`）
- 前端代码根路径（示例：`frontend/src/...`）
- 数据库 migration 根路径
- 文档根路径（`docs/`）

> 若某个目标目录尚不存在，先创建“脚手架/模块初始化”任务，并将 `refs` 指向其父级真实存在目录；禁止直接引用仓库中不存在的深层目标路径。

### Step 2：功能→Feature Task 分解

为每个较大的功能，创建一个 Feature Task 文件：

文件路径：`phases/phase-XX/feature-tasks/FT-001-<slug>.md`

使用模板：`templates/feature-task.md`

### Step 3：Feature Task→原子任务分解

将每个 Feature Task 中的工作进一步拆分为原子任务。

使用 `sequential-thinking`（totalThoughts: 6-8）进行拆分：
```
thought 1: 列出当前阶段所有功能点
thought 2: 为每个功能点拆分数据库任务
thought 3: 为每个功能点拆分后端任务
thought 4: 为每个功能点拆分前端任务
thought 5: 为每个功能点补充测试任务
thought 6: 确定任务依赖关系和优先级
thought 7: 分配 Agent 和估算工时
thought 8: 验证任务完整性（无遗漏）
```

#### 3.1 演进任务强制补齐 (Evolution Gates)

1. **数据库结构变更门禁**（新增/修改/删除表、索引、约束）：
   - 必须创建 `database` 任务：编写 `database/migrations/V<phase><seq>__<slug>.sql`
   - 必须创建 `docs` 任务：更新 `docs/database-schema.md`
   - 依赖规则：所有依赖新结构的 `backend/test-integration/test-e2e` 任务必须依赖上述 migration 任务
2. **接口与架构变更门禁**（新增/修改 API、DTO、领域模型、模块边界）：
   - 必须创建 `docs` 任务：更新 `docs/api-contracts.md`
   - 若模块边界/组件关系变化，必须额外创建 `docs` 任务更新 `docs/architecture.md`
3. **本地依赖变更门禁**（引入 Redis/MQ/ES 等）：
   - 必须先创建 `config` 任务：更新 `docker-compose.dev.yml`、必要 `.env.example` 配置
   - 必须创建验证任务：`docker compose -f docker-compose.dev.yml up -d` + `docker compose -f docker-compose.dev.yml ps`
   - 依赖规则：相关业务任务必须依赖该 `config` 任务
4. **前端任务原型绑定门禁**（当 `docs/prototype-map.md` 存在时生效）：
   - 每个 `area=frontend` 且 `task_type=frontend` 的任务，`refs` 字段**必须**包含对应原型文件路径（如 `docs/prototypes/login.png`）
   - Feature Task 文件必须包含**原型参考**章节，标注该功能对应的原型页面及关键 UI 要素
   - 若原型中存在但 CSV 中无对应前端任务的页面，必须补充任务（防止遗漏）
   - `acceptance_criteria` 摘要中必须包含「与原型一致」相关描述
5. **前端导航闭环门禁**：
   - 每个前端页面任务的 `acceptance_criteria` 必须包含导航闭环验收项：「页面提供返回/回退入口，无导航死胡同」
   - 涉及路由新增的任务，必须在 Feature Task 中标注该路由的**入口来源**和**出口返回路径**
   - 涉及错误页面（404/403/500）的任务，`acceptance_criteria` 必须包含「提供回到首页的导航入口」
   - 涉及表单/弹窗/抽屉的任务，`acceptance_criteria` 必须包含「提供关闭/取消操作」
   - 若 `docs/architecture.md` 中存在导航闭环矩阵，任务拆分须逐条对照，确保每个路由均有对应前端任务覆盖其闭环要求
6. **API 合约生成门禁**（多 Agent 并行模式下必须执行，单 Agent 模式可选）：
   - 当项目为前后端分离架构时，必须为每个 Phase 生成 API 合约 YAML（OpenAPI 3.0）到 `docs/api-contracts/phase-XX-<slug>.yaml`
   - 初始化 `docs/api-contracts/CHANGELOG.md`（使用 `templates/api-changelog.md` 模板）
   - 前端任务的 `contract_version` 字段必须填写当前合约版本
   - 后端任务与前端任务之间的依赖关系必须通过 API 合约解耦，而非直接代码依赖

### Step 4：生成 todolist.csv

**CSV 表头（固定）**：

```csv
"id","priority","phase","module","area","task_type","title","description","acceptance_criteria","val_command","dependencies","estimated_hours","assigned_agent","attempts","started_at_commit","dev_state","test_state","review_state","git_state","refs","notes"
```

**字段说明**：

| 字段 | 说明 | 示例值 |
|------|------|-------|
| `id` | 唯一标识，`PH<XX>-<NNN>` 格式 | `PH01-010` |
| `priority` | 优先级 | `P0` / `P1` / `P2` |
| `phase` | 阶段编号 | `1` |
| `module` | 所属模块 | `auth` / `user` |
| `area` | 前后端 | `frontend` / `backend` / `both` |
| `task_type` | 任务类型 | `backend` / `frontend` / `test-unit` |
| `title` | 简短标题 | `用户登录 API` |
| `description` | 单行摘要（建议 ≤ 80 字） | `实现 POST /api/v1/auth/login` |
| `acceptance_criteria` | 单行验收摘要（详细版写入 Feature Task） | `返回 JWT Token; 错误密码返回 401` |
| `val_command` | **单行机械校验入口命令**（复杂逻辑放脚本） | `npm test -- --testPathPattern=login` 或 `pwsh scripts/validation/PH01-020.ps1` |
| `dependencies` | 前置任务 ID | `PH01-010;PH01-020` |
| `estimated_hours` | 预估工时 | `2` |
| `assigned_agent` | 分配 Agent | `codex` / `gemini` / `claude` |
| `attempts` | **重试次数** | `0` |
| `started_at_commit`| **任务开始的 Git Hash** | 空 |
| `dev_state` | 开发状态 | `未开始` / `进行中` / `已完成` / `失败` |
| `test_state` | 测试状态 | `未开始` / `进行中` / `已完成` |
| `review_state` | 审查状态 | `未开始` / `进行中` / `已完成` |
| `git_state` | 提交状态 | `未提交` / `已提交` |
| `refs` | 引用文件 | `src/auth/login.py:1` |
| `notes` | 备注 | 空 |
| `role` | （多 Agent 可选）角色归属 | `backend` / `frontend` / `audit` |
| `contract_version` | （多 Agent 可选）API 合约版本 | `v1.0.0` |

**默认值**：生成时 `attempts` = `0`，`started_at_commit` = 空，`dev_state`/`test_state`/`review_state` = `未开始`，`git_state` = `未提交`。

**CSV 文本约束（防转义脆弱）**：
- `description` / `acceptance_criteria` / `val_command` 禁止换行。
- 避免内嵌双引号与多行 JSON；如需复杂说明，写入 `feature-tasks/*.md` 并在 `refs` 引用。
- `acceptance_criteria` 保持摘要化，详细验收标准以 Feature Task 为准。

**ID 规则**：以 10 递增（`PH01-010`, `PH01-020`, ...），方便后续插入。

### Step 5：生成 process.md

使用模板 `templates/process.md`，初始化当前阶段的进度文件。

### Step 6：验证 CSV

```powershell
# 验证 CSV 可解析
Import-Csv "phases/phase-XX/todolist.csv" | Format-Table id, title, area, assigned_agent

# 检查行数
(Import-Csv "phases/phase-XX/todolist.csv").Count
```

验证要点：
- [ ] 所有字段填充完整（无空 acceptance_criteria）
- [ ] 依赖关系无循环
- [ ] 每个功能点有 前端+后端+测试 覆盖
- [ ] ID 唯一且递增
- [ ] Agent 分配合理
- [ ] `val_command` 满足安全断言（无破坏性命令、无生产环境访问）
- [ ] `refs` 仅引用仓库中真实存在的路径，或引用真实存在父目录下的计划新文件
- [ ] 涉及 DB 结构变更时，存在 migration + `docs/database-schema.md` 更新任务
- [ ] 涉及 API/模型/架构变更时，存在 `docs/api-contracts.md`（及必要时 `docs/architecture.md`）更新任务
- [ ] 涉及新中间件时，存在 `docker-compose.dev.yml` 更新与启动验证任务，且被相关业务任务依赖
- [ ] CSV 长文本已外置到 Feature Task，CSV 字段无多行内容
- [ ] （若有原型）所有前端任务 `refs` 已关联对应原型文件
- [ ] （若有原型）Feature Task 已包含原型参考章节
- [ ] （多 Agent 模式）API 合约 YAML 已生成且 `CHANGELOG.md` 已初始化
- [ ] （多 Agent 模式）前端任务已填写 `contract_version` 字段
- [ ] （多 Agent 模式）后端与前端任务通过 API 合约解耦，无直接代码依赖

## 五、完成条件

- [ ] `phases/phase-XX/todolist.csv` 已生成且可解析
- [ ] `phases/phase-XX/process.md` 已初始化
- [ ] Feature Task 文件已创建（至少 1 个）
- [ ] CSV 行数在合理范围（10-80 行/阶段）
- [ ] 所有任务有明确的验收标准
- [ ] 依赖关系合理且无循环

## 六、输出到下一 Stage

- `phases/phase-XX/todolist.csv`
- `phases/phase-XX/process.md`
- `phases/phase-XX/feature-tasks/*.md`
