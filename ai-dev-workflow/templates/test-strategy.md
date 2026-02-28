# 测试策略

> 本文件由 Stage 1 (架构设计) 的 Step 4.5 生成，定义项目整体测试方案。

## 一、测试金字塔

```
        ┌────────┐
        │  E2E   │  10%  — 核心用户流程端到端验证
        ├────────┤
        │ 集成测试 │  20%  — API 层 + 数据库交互验证
        ├────────┤
        │ 单元测试 │  70%  — 业务逻辑 + 工具函数验证
        └────────┘
```

## 二、覆盖率目标

| 层级 | 覆盖率目标 | 强制执行 |
|------|-----------|---------|
| 后端单元测试 | ≥ 80% 行覆盖率 | ✅ CI 阻断 |
| 前端组件测试 | ≥ 60% 行覆盖率 | ✅ CI 阻断 |
| API 集成测试 | 核心接口 100% 覆盖 | ⚠️ 建议 |
| E2E 测试 | 核心用户路径 100% 覆盖 | ⚠️ 建议 |

## 三、性能基线

> 来自 NFR Matrix (`docs/specification.md`)

| 指标 | 基线值 | 测试方法 |
|------|--------|---------|
| API P95 响应时间 | < 200ms | 负载测试 (k6 / JMeter) |
| 页面首屏加载 | < 2s | Lighthouse |
| 数据库单表查询 | < 50ms | SQL EXPLAIN 分析 |
| 内存占用 (后端) | < 512MB (空载) | Docker stats |

## 四、测试命名约定

| 技术栈 | 命名格式 | 示例 |
|--------|---------|------|
| Java | `*Test.java` / `*IntegrationTest.java` | `UserServiceTest.java` |
| Node.js | `*.test.ts` / `*.spec.ts` | `user.service.test.ts` |
| Python | `test_*.py` | `test_user_service.py` |
| React/Vue | `*.test.tsx` / `*.spec.ts` | `LoginForm.test.tsx` |

## 五、测试数据策略

- **单元测试**：使用 Mock/Stub，不依赖外部服务
- **集成测试**：使用 Testcontainers 或内存数据库
- **E2E 测试**：使用隔离的测试数据库 + Seed 数据

## 六、回归测试策略

每个 Phase 完成后，Stage 5 审查时必须：
1. 运行 **全量** 单元测试和集成测试（不仅是本 Phase 新增的）
2. 覆盖率不得低于前一个 Phase 的值（只升不降）
3. 如覆盖率下降，在审查报告中标记为 ⚠️ 需关注项

## 七、测试工具推荐

| 技术栈 | 单元测试 | 集成测试 | E2E 测试 |
|--------|---------|---------|---------|
| Java (Spring Boot) | JUnit 5 + Mockito | Spring Boot Test + Testcontainers | — |
| Node.js (NestJS) | Jest + ts-jest | Supertest + Testcontainers | Playwright |
| Python (FastAPI) | pytest + unittest.mock | httpx + pytest-asyncio | Playwright |
| React | React Testing Library + Jest | — | Playwright / Cypress |
| Vue | Vitest + Vue Test Utils | — | Playwright / Cypress |
