# Pattern: LLM Route Fallback on Timeout / 429

## 场景
- 前端任务按策略优先 Gemini；Gemini 调用可能超时无输出或返回 429 容量错误。

## 判定规则
- 满足以下任一条件立即回退主执行代理：
  - 连续轮询无输出且超过设定超时；
  - 返回 429 / `MODEL_CAPACITY_EXHAUSTED`；
  - 非成功退出码。

## 执行动作
1. 记录失败原因（`gemini_fallback:*`）到任务 `notes` 与 `memory`。
2. 由 Codex 继续实现并完成验证。
3. 保持同一任务 ID，不拆新任务，保证交付链路可追溯。

## 收益
- 避免等待外部模型恢复导致流程阻塞。
- 保持交付确定性和可审计性。

