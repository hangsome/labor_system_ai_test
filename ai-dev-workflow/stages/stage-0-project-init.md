---
stage: 0
name: 项目初始化
description: 输入物料校验 → 需求分析 → 技术栈选型 → 项目脚手架生成 → Git 初始化
input: 产品原型图 和/或 需求文档（至少提供一项）
output: 项目骨架 + docs/tech-stack.md
---

# Stage 0：项目初始化

## 一、目标

根据用户需求，选择合适的技术栈，生成项目脚手架，为后续开发奠定基础。

## 二、AI 行为约定

1. **不直接写业务代码**，只生成项目骨架和配置。
2. **遵守主干分支保护隔离**：AI 不得在 `main`（或 `master`）分支上直接执行阶段性任务的原子提交。除了最初始化的脚手架（Skeleton）创建，禁止一切直接 PUSH 主分支的行为。
3. **必须使用 `sequential-thinking` MCP** 进行技术栈分析与选型推理。
4. 使用 `context7` 查询框架/库的官方文档，确保选型基于准确的 API 信息；并利用 `web-search` 调研技术方案。
5. 技术栈偏好遵循用户设定（前端 React/Vue、后端 Java/Python/Node.js、数据库 MySQL），但可根据实际需求推荐其他方案（需说明理由）。

## 三、执行步骤

### Step 0：Plan 文件消化与输入物料校验

工作流启动时，必须确认用户提供了 **Plan 文件**（使用 `templates/plan.md` 模板）或自然语言描述：

| 物料 | 状态 | 格式说明 |
|------|------|----------|
| **Plan 文件** | ✅/❌ | 填写完成的 `plan.md`，包含系统定位 / 模块列表 / 阶段划分 / 技术偏好 |
| 产品原型图 | ✅/❌ | Figma 链接 / Axure 导出 / 截图附件 |
| 需求文档 | ✅/❌ | PRD / 功能列表 / 用户故事 / Markdown |

**处理规则**：
- 如果提供了 Plan 文件 → 解析其中的功能模块和阶段划分，作为后续 Stage 1/2 的事实基础。
- 如果只有自然语言描述 → AI 自动生成 Plan 文件，写入 `docs/plan.md` 并要求用户确认后再继续。
- AI 应主动使用 `web-search` 和 `context7` 補充调研，補全用户未提供的技术细节。

**原型基线建立（自动扫描 + 产品对齐度保障）**：

AI 必须**主动扫描**项目目录，检测是否存在产品原型或 UI 相关需求文档：

**扫描范围**（按优先级）：
- `docs/prototypes/`、`docs/design/`、`design/`、`mockups/`、`ui/` 等常见原型目录
- `docs/` 下的 PRD、需求文档（`*.md`/`*.pdf`/`*.docx` 中含页面描述或 UI 规格的文件）
- 项目根目录下的原型图片（`*.png`/`*.jpg`/`*.svg`/`*.fig`/`*.sketch`/`*.xd`）
- 用户在 Plan 文件或自然语言中提供的 Figma 链接、截图附件等

**发现原型后，自动执行以下步骤**：

1. 将散落的原型文件统一归档到 `docs/prototypes/` 目录（按页面/模块命名）
2. 生成 `docs/prototype-map.md`——原型页面清单与功能模块映射表：
   ```markdown
   # 产品原型映射表

   | 原型文件 | 页面/视图 | 对应模块 | 关键 UI 要素 |
   |---------|----------|---------|-------------|
   | prototypes/login.png | 登录页 | auth | 表单布局、品牌 Logo、第三方登录按钮 |
   | prototypes/dashboard.png | 仪表盘 | dashboard | 统计卡片、图表区域、侧边栏 |
   ```
3. 标注每个原型页面的 **关键 UI 要素**（布局结构、配色方案、交互行为）
4. 要求用户确认映射关系的准确性

> 📌 原型基线是后续 Stage 3/4/5 产品对齐度校验的事实基础。AI 必须主动发现原型，而非等待用户显式提供。

**Pattern Library 查询（知识复用）**：

在消化 Plan 后、进入 Specification 之前，AI 必须检查 `patterns/` 目录：

1. 扫描 `patterns/` 目录是否存在（首个项目可能为空）
2. 如果存在，遍历各分类目录，匹配与当前系统相关的 Pattern：
   - 按系统类型匹配（如 Web 应用 → 查找 RBAC、JWT、CRUD 等 Pattern）
   - 按业务领域匹配（如电商 → 查找购物车、支付等 Pattern）
3. 将匹配到的 Pattern 列表输出给用户确认是否采用
4. 被采用的 Pattern 在后续 Stage 1 架构设计时作为参考输入

> 📌 Pattern Library 是跨项目的知识资产。首个项目不会有 Pattern，但从第二个项目开始就能受益。

### Step 0.5：需求规格化 (Specification)

> 此步骤是 SDD (Specification-Driven Development) 的核心。
> Spec 越精确，后续 AI 生成的代码质量越高。

**目的**：将粗粒度的功能描述细化为可机械验证的规格。

**执行动作**：

1. **用户故事提取**：
   - 从 Plan 文件的每个模块中提取用户故事
   - 每个故事必须有 GIVEN-WHEN-THEN 格式的验收标准
   - 调用 `sequential-thinking`（totalThoughts: 4-6）辅助提取

2. **业务规则提炼**：
   - 识别隐含的业务规则（如权限控制、数据校验、状态流转）
   - 将口语化描述转换为 IF-THEN 结构化规则

3. **NFR 量化**：
   - 如 Plan 文件中未填写 NFR Matrix，AI 应根据系统类型和规模推荐合理默认值
   - 必须确认性能、安全、兼容性三个维度

4. **数据词典建立**：
   - 提取核心业务实体的名称和定义
   - 确保术语在前后端、数据库中保持一致（如中文术语对应英文字段名）

5. **输出**：`docs/specification.md`（如 Plan 中已内联 Spec 内容，则从 Plan 提取生成独立文件）

**完成条件**：
- [ ] 每个功能模块至少 2 个用户故事
- [ ] 每个用户故事至少 1 条 GIVEN-WHEN-THEN 验收标准
- [ ] NFR Matrix 已量化关键指标
- [ ] 业务规则已结构化
- [ ] 用户确认 Specification

### Step 1：需求分析

1. 解析用户需求描述，提取以下信息：
   - 系统类型（Web 应用 / 管理平台 / API 服务 / 微服务等）
   - 核心业务领域（电商 / 企业管理 / 社交 / 内容管理等）
   - 用户规模预估（小型 / 中型 / 大型）
   - 特殊需求（实时通讯 / 文件上传 / 支付 / 地图等）

2. 调用 `sequential-thinking`（totalThoughts: 4-6）分析需求复杂度：
   - `thought 1`：需求归纳与系统定位
   - `thought 2`：技术约束识别（性能 / 安全 / 扩展性）
   - `thought 3`：技术栈推荐与理由
   - `thought 4`：项目结构设计

### Step 2：技术栈选型

根据需求分析结果，从以下矩阵中选择技术栈：

**前端选型**：

| 场景 | 推荐框架 | 理由 |
|------|---------|------|
| 中后台管理系统 | Vue 3 + Element Plus / Ant Design Vue | 组件库丰富、上手快 |
| 复杂交互 SPA | React + Ant Design / Material UI | 生态丰富、灵活度高 |
| 全栈同构 | Next.js (React) / Nuxt.js (Vue) | SSR/SSG 支持 |
| 简单展示站 | Vue 3 | 轻量级、学习曲线低 |

**后端选型**：

| 场景 | 推荐技术 | 理由 |
|------|---------|------|
| 企业级应用 | Java (Spring Boot) | 稳定、生态完善、适合大团队 |
| 快速原型 / API 服务 | Node.js (Express/NestJS) | 开发速度快、前后端统一 |
| 数据密集型 / AI 集成 | Python (FastAPI/Django) | 数据处理能力强 |
| 微服务架构 | Java + Spring Cloud | 成熟的微服务生态 |

**数据库**：MySQL（用户指定），辅以 Redis（缓存，按需）。

### Step 3：项目脚手架生成

根据选定技术栈，生成项目目录结构：

```
<project-root>/
├── docs/                       # 文档目录
│   └── tech-stack.md           # 技术栈说明（本 Stage 输出）
├── src/
│   ├── frontend/               # 前端项目
│   │   ├── package.json
│   │   ├── src/
│   │   └── ...
│   └── backend/                # 后端项目
│       ├── pom.xml / package.json / requirements.txt
│       ├── src/
│       └── ...
├── database/                   # 数据库脚本
│   └── init.sql
├── tests/                      # 测试目录
├── phases/                     # 阶段文件夹（后续 Stage 生成）
├── .gitignore
└── README.md
```

**使用框架脚手架命令**（按技术栈选择）：

- React: `npx -y create-react-app ./src/frontend` 或 `npx -y create-vite@latest ./src/frontend -- --template react-ts`
- Vue: `npx -y create-vue@latest ./src/frontend`
- Spring Boot: 使用 Spring Initializr 或手动创建 Maven/Gradle 项目
- Node.js: `npx -y express-generator ./src/backend` 或 `npx -y @nestjs/cli new ./src/backend`
- Python: 创建 `requirements.txt` + FastAPI/Django 项目结构

### Step 4：初始化配置与首个 Baseline

1. 初始化 Git 仓库：`git init` 并建立 `dev` 分支（`git checkout -b dev`），同时创建 `prod` 分支（`git branch prod`）。
2. 生成 `.gitignore`（根据技术栈）。
3. **Branch Auto-Adapt Protocol**（仅对已有项目）：检测现有分支结构并自动调整为 `dev`/`prod` 模型：
   - 仅有 `main` → 重命名为 `dev`，创建 `prod`
   - `main` + `develop` → 映射 `develop`→`dev`，`main`→`prod`
   - 已有 `dev` + `prod` → 直接使用
   - 其他结构 → 提示用户确认分支映射
3. 配置数据库连接模板。
4. 生成 `docs/tech-stack.md`。
5. 创建本地开发依赖基线：生成 `docker-compose.dev.yml`（至少含 MySQL/Redis 等集成测试依赖）。
6. 启动并验证本地依赖服务：
   - `docker compose -f docker-compose.dev.yml up -d`
   - `docker compose -f docker-compose.dev.yml ps` 确认服务为 `running/healthy`
7. **Shift-Left 基线**：创建最小 CI 骨架（建议 `.github/workflows/ci-baseline.yml`），至少包含：
   - lint
   - unit test
   - secrets scan（如 gitleaks/trufflehog）
8. 提交基础框架脚手架（Initial skeleton commit）：
   ```bash
   git add .
   git commit -m "chore: Initialize project skeleton, tech stack docs and ci baseline"
   ```

> 说明：`docker-compose.dev.yml` 是可演进基线。后续 Phase 如引入新中间件，必须在 Stage 3 先创建 `config` 任务更新并验证该文件，再进入业务开发任务。

### Step 5：输出 tech-stack.md

文件内容模板：

```markdown
# 技术栈说明

## 系统概述
<一句话描述系统定位>

## 技术选型

### 前端
- 框架：<React/Vue + 版本>
- UI 库：<Element Plus / Ant Design 等>
- 构建工具：<Vite / Webpack>

### 后端
- 语言：<Java/Python/Node.js + 版本>
- 框架：<Spring Boot / FastAPI / NestJS>
- ORM：<MyBatis / Sequelize / SQLAlchemy>

### 数据库
- 主库：MySQL 8.x
- 缓存：Redis（按需）

### 开发工具
- 版本管理：Git
- 包管理：<npm/yarn / Maven/Gradle / pip>

## 目录结构
<项目目录树>

## 开发环境要求
<Node.js / JDK / Python 版本要求>
```

## 四、完成条件

- [ ] 项目目录结构已生成
- [ ] 前端项目可启动（`npm run dev` 或等效命令）
- [ ] 后端项目可编译/启动
- [ ] `docs/tech-stack.md` 已生成
- [ ] `docker-compose.dev.yml` 已创建，且本地依赖服务可正常启动
- [ ] 最小 CI 基线已创建并可运行
- [ ] Git 仓库已初始化，`dev` 和 `prod` 分支已创建
- [ ] 用户确认技术栈选型
- [ ] （若提供原型）`docs/prototypes/` 已归档原型文件
- [ ] （若提供原型）`docs/prototype-map.md` 已生成且用户确认映射关系

## 五、输出到下一 Stage

- `docs/tech-stack.md`
- 项目目录骨架
- 确认的技术栈信息
