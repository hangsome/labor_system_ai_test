# Pattern: LLM Route Fallback on Timeout / 429

## 场景
- 前端任务按策略优先 Gemini，降级链固定为 `Gemini -> Claude -> Codex`。
- Gemini/Claude 调用可能超时无输出或返回 429 容量错误。

## 判定规则
- 满足以下任一条件立即回退到下一降级代理：
  - 连续轮询无输出且超过设定超时；
  - 返回 429 / `MODEL_CAPACITY_EXHAUSTED`；
  - 非成功退出码。

## 执行动作
1. Gemini 失败时，记录 `gemini_fallback:*` 到任务 `notes` 与 `memory`，并立即改由 Claude 执行。
2. Claude 失败时，记录 `claude_fallback:*` 到任务 `notes` 与 `memory`，并立即改由 Codex 执行。
3. 保持同一任务 ID，不拆新任务，保证交付链路可追溯。

## 收益
- 避免等待单一外部模型恢复导致流程阻塞。
- 在不牺牲交付速度的前提下，保留二次模型审视机会（Claude）。
- 保持交付确定性和可审计性。

