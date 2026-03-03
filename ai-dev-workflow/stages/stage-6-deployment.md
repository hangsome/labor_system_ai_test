---
stage: 6
name: 部署与运维就绪
description: CI/CD 生产化增强 + 容器化 + 环境管理 + 安全扫描 + 监控配置 + 部署文档
input: 通过所有 Phase 验收的完整代码
output: CI/CD 配置 + Dockerfile + 部署文档 + 监控配置
---

# Stage 6：部署与运维就绪

## 一、目标

在 Stage 0 已建立最小 CI 基线的前提下，完成生产环境发布所需的部署与运维增强能力。

> 本 Stage 在所有 Phase 完成后执行一次。

## 二、AI 行为约定

1. **所有配置文件必须可直接运行**：不使用无意义占位流程。
2. **安全第一**：敏感信息必须通过环境变量或 Secrets 管理，禁止硬编码。
3. **环境隔离**：dev / staging / production 三套环境隔离。
4. **必须使用 `sequential-thinking`**（totalThoughts: 6-8）规划部署架构。

## 三、执行步骤

### Step 1：部署架构规划

使用 `sequential-thinking` 分析：组件依赖、部署拓扑、流水线阶段、监控与回滚策略。

### Step 2：容器化

生成 Dockerfile（多阶段构建 + 健康检查）与 docker-compose.yml。

必备服务（按需裁剪）：
- frontend
- backend
- database
- redis（可选）

Dockerfile 要求：
- 多阶段构建
- 非 root 用户运行
- 健康检查 `HEALTHCHECK`

Compose 要求：
- `.env` 注入变量
- 数据库 healthcheck
- 服务依赖与网络隔离
- 数据卷持久化

### Step 3：CI/CD 流水线（生产化增强）

生成 `.github/workflows/ci.yml`（或用户指定平台）

| Job | 触发 | 动作 | 依赖 |
|-----|------|------|------|
| backend-test | push / PR | lint + unit + coverage | - |
| frontend-test | push / PR | lint + component test + coverage | - |
| security-scan | push / PR | 依赖漏洞 + secrets + SAST | - |
| build | push to main | 构建镜像 | tests + security |
| deploy-staging | push to main | 自动部署到 staging | build |
| deploy-production | release tag | 人工批准后部署 | build |

CI 规则：
1. `actions/cache` 缓存依赖。
2. 覆盖率结果上传 Artifact。
3. **安全门一致性**：
   - high / critical 漏洞与 secrets 泄露 -> 阻断流水线
   - SAST 中低风险 -> 告警并跟踪
4. 禁止使用全局 `continue-on-error: true` 规避安全阻断。

**多 Agent 分支 CI 适配**（多 Agent 并行模式下生效）：
- 后端分支 (`feature/phase-XX-*` 不含 `-fe`) 仅触发 `backend-test` + `security-scan`
- 前端分支 (`feature/phase-XX-*-fe`) 仅触发 `frontend-test` + `security-scan`
- `main` 分支触发全量 CI（前后端 + 构建 + 部署）
- 实现方式：使用 `paths` 过滤或 `if` 条件判断分支名

```yaml
# 示例：路径过滤
on:
  push:
    paths:
      - 'backend/**'    # 触发 backend-test
      - 'frontend/**'   # 触发 frontend-test
```

### Step 4：环境管理

生成：
- `.env.example`（可提交）
- `.env.development`（gitignore）
- `.env.staging`（gitignore）
- `.env.production`（gitignore）

### Step 5：数据库迁移策略

1. 校验 `database/migrations/` 已在各 Phase 持续演进（而非仅在 Stage 6 一次性补写）。
2. 核对 migration 链路与 `docs/database-schema.md` 一致性，识别缺失脚本与顺序断裂。
3. 若存在缺失 migration 或 schema drift：**阻断发布**，回退到 Stage 3/4 补齐后再进入 Stage 6。
4. 仅允许在用户明确批准下执行一次性补偿迁移，并在 `docs/deployment.md` 记录风险与回滚方案。

### Step 6：监控与健康检查

最小监控基线：
- `GET /health`
- `GET /health/ready`

可选增强：Prometheus + Grafana（通过独立 compose overlay 启动）。

### Step 7：安全扫描集成

| 检查项 | 工具 | 阻断级别 |
|--------|------|---------|
| 依赖漏洞 | npm audit / dependency-check | high/critical 阻断 |
| Secrets 泄露 | TruffleHog / Gitleaks | 阻断 |
| SAST | ESLint security / SpotBugs | 中高风险阻断，低风险告警 |
| 镜像扫描 | Trivy | critical 阻断 |
| OWASP Top 10 | Claude 审计（Stage 5） | 阻断 |

### Step 8：生成部署文档

输出 `docs/deployment.md`，包含：
- 快速启动
- 环境说明
- CI/CD 说明
- 回滚方案

### Step 9：最终提交

```bash
git add .
git commit -m "chore: harden deployment infrastructure and production readiness"
```

## 四、完成条件

- [ ] Dockerfile 可构建
- [ ] docker-compose 可启动
- [ ] CI/CD 配置可运行
- [ ] `.env.example` 完整
- [ ] 迁移脚本已在各 Phase 版本化沉淀（非 Stage 6 临时补写）
- [ ] migration 链路与 `docs/database-schema.md` 无漂移
- [ ] `docs/deployment.md` 已生成
- [ ] 安全扫描门禁可执行
- [ ] 用户确认部署方案

## 五、输出

- `Dockerfile`（前后端）
- `docker-compose.yml`
- `.github/workflows/ci.yml`（或等效）
- `.env.example`
- `database/migrations/`
- `docs/deployment.md`
- `monitoring/`（可选）

## 六、最终交付

- 完整部署基础设施
- 可一键启动的开发环境
- 自动化 CI/CD 流水线
- 安全合规基线

