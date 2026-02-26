# Pattern: Vitest Unit Scope Separation

## 场景
- 项目同时存在 `vitest` 单测和 `playwright` E2E，且文件名均为 `*.spec.ts`。

## 症状
- 执行 `npm run test` 时，`vitest` 错误加载 E2E 用例并报 `Playwright Test did not expect test.beforeEach()`.

## 方案
- 在 `vitest.config.ts` 中将 `include` 收敛到单测目录，例如：
  - `include: ['tests/unit/**/*.spec.ts']`
- E2E 独立命令执行：
  - `npx playwright test tests/e2e/<spec>.ts`

## 收益
- 测试执行器职责分离，避免工具链冲突，减少回归噪音。

