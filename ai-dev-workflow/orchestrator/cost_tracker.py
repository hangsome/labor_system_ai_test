from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any


# USD per 1M tokens, rough defaults for budget guardrails.
DEFAULT_PRICING_PER_MTOKEN: dict[str, tuple[float, float]] = {
    "gpt-4o": (5.0, 15.0),
    "gpt-4o-mini": (0.15, 0.6),
    "claude-sonnet-4-20250514": (3.0, 15.0),
    "gemini-2.5-pro": (3.5, 10.0),
}


@dataclass
class CostTracker:
    total_cost_usd: float = 0.0
    usage_events: list[dict[str, Any]] = field(default_factory=list)

    def _lookup_price(self, model: str) -> tuple[float, float]:
        if model in DEFAULT_PRICING_PER_MTOKEN:
            return DEFAULT_PRICING_PER_MTOKEN[model]
        lower = model.lower()
        for key, value in DEFAULT_PRICING_PER_MTOKEN.items():
            if key in lower:
                return value
        # Conservative fallback
        return (5.0, 15.0)

    def add_usage(
        self,
        model: str,
        prompt_tokens: int | None,
        completion_tokens: int | None,
    ) -> float:
        prompt = int(prompt_tokens or 0)
        completion = int(completion_tokens or 0)
        in_price, out_price = self._lookup_price(model)
        cost = (prompt / 1_000_000.0) * in_price + (completion / 1_000_000.0) * out_price
        self.total_cost_usd += cost
        self.usage_events.append(
            {
                "model": model,
                "prompt_tokens": prompt,
                "completion_tokens": completion,
                "cost_usd": round(cost, 8),
            }
        )
        return cost

    def model_dump(self) -> dict[str, Any]:
        return {
            "total_cost_usd": round(self.total_cost_usd, 8),
            "usage_events": self.usage_events,
        }

    @classmethod
    def from_payload(cls, payload: dict[str, Any] | None) -> "CostTracker":
        if not payload:
            return cls()
        tracker = cls()
        tracker.total_cost_usd = float(payload.get("total_cost_usd", 0.0))
        tracker.usage_events = list(payload.get("usage_events", []))
        return tracker

