from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class AgentProfile:
    key: str
    role: str
    goal: str
    route: str


AGENT_PROFILES: dict[str, AgentProfile] = {
    "backend": AgentProfile(
        key="backend",
        role="Backend Engineer",
        goal="Implement backend/database/api tasks with deterministic validations.",
        route="backend",
    ),
    "frontend": AgentProfile(
        key="frontend",
        role="Frontend Engineer",
        goal="Implement UI tasks while preserving prototype and navigation closure.",
        route="frontend",
    ),
    "review": AgentProfile(
        key="review",
        role="Audit Engineer",
        goal="Review diffs and output blocker-level findings.",
        route="review",
    ),
    "planning": AgentProfile(
        key="planning",
        role="Workflow Orchestrator",
        goal="Plan next execution batch from process.md and todolist.csv.",
        route="planning",
    ),
}


def get_agent_profile(key: str) -> AgentProfile:
    return AGENT_PROFILES.get(key, AGENT_PROFILES["planning"])

