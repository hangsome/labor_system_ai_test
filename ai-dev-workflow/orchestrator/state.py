from __future__ import annotations

from datetime import datetime, timezone
from typing import Any
from uuid import uuid4

from pydantic import BaseModel, Field


def now_iso() -> str:
    return datetime.now(timezone.utc).astimezone().isoformat()


class ProjectState(BaseModel):
    """State snapshot persisted to `.crewai/flows/<flow-id>.json`."""

    flow_id: str = Field(default_factory=lambda: uuid4().hex)
    mode: str = "hybrid"
    current_stage: int = 0
    current_phase: int = 1
    total_phases: int = 0
    stage4_rounds: int = 0
    tasks_completed: list[str] = Field(default_factory=list)
    tasks_failed: list[str] = Field(default_factory=list)
    api_cost_usd: float = 0.0
    outputs: list[str] = Field(default_factory=list)
    last_error: str = ""
    created_at: str = Field(default_factory=now_iso)
    updated_at: str = Field(default_factory=now_iso)

    def touch(self) -> None:
        self.updated_at = now_iso()

    def mark_stage(self, stage: int) -> None:
        self.current_stage = max(0, int(stage))
        self.touch()

    def add_output(self, path: str) -> None:
        if path not in self.outputs:
            self.outputs.append(path)
        self.touch()

    def bump_stage4_round(self) -> int:
        self.stage4_rounds += 1
        self.touch()
        return self.stage4_rounds

    def record_task(self, task_id: str, success: bool) -> None:
        if success:
            if task_id not in self.tasks_completed:
                self.tasks_completed.append(task_id)
            if task_id in self.tasks_failed:
                self.tasks_failed.remove(task_id)
        else:
            if task_id not in self.tasks_failed:
                self.tasks_failed.append(task_id)
        self.touch()

    @classmethod
    def from_json_data(cls, payload: dict[str, Any]) -> "ProjectState":
        return cls.model_validate(payload)

