from __future__ import annotations

import os
from pathlib import Path
from typing import Any

import yaml
from crewai import Agent, Crew, LLM, Process, Task


def _read_yaml(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as file:
        data = yaml.safe_load(file) or {}
    if not isinstance(data, dict):
        raise ValueError(f"Invalid YAML structure: {path}")
    return data


def _normalize_base_url(value: str | None) -> str | None:
    text = (value or "").strip().rstrip("/")
    if not text:
        return None
    if text.endswith("/v1"):
        return text
    return f"{text}/v1"


def _build_llm(model: str, base_url: str | None, api_key: str | None) -> LLM | None:
    normalized_base = _normalize_base_url(base_url)
    if not model or not normalized_base or not api_key:
        return None
    return LLM(
        model=model,
        base_url=normalized_base,
        api_key=api_key,
        temperature=0.2,
    )


def build_workflow_crew(config_root: Path, verbose: bool = True) -> Crew:
    agents_cfg = _read_yaml(config_root / "agents.yaml")
    tasks_cfg = _read_yaml(config_root / "tasks.yaml")

    planning_model = os.getenv("CREWAI_MODEL_ORCHESTRATOR") or os.getenv("MODEL_PLANNING") or "gpt-5.2-codex"
    backend_model = os.getenv("CREWAI_MODEL_BACKEND") or os.getenv("MODEL_BACKEND") or "gpt-5.2-codex"
    frontend_model = os.getenv("CREWAI_MODEL_FRONTEND") or os.getenv("MODEL_FRONTEND") or "gemini-3-flash-preview"
    audit_model = os.getenv("CREWAI_MODEL_AUDIT") or os.getenv("MODEL_REVIEW") or "claude-opus-4-6"

    planning_llm = _build_llm(
        planning_model,
        os.getenv("CREWAI_API_BASE") or os.getenv("OPENAI_BASE_URL") or os.getenv("OPENAI_API_BASE"),
        os.getenv("CREWAI_API_KEY") or os.getenv("OPENAI_API_KEY"),
    )
    backend_llm = _build_llm(
        backend_model,
        os.getenv("CREWAI_BACKEND_API_BASE") or os.getenv("OPENAI_API_BASE"),
        os.getenv("CREWAI_BACKEND_API_KEY") or os.getenv("OPENAI_API_KEY"),
    )
    frontend_llm = _build_llm(
        frontend_model,
        os.getenv("CREWAI_FRONTEND_API_BASE") or os.getenv("GEMINI_API_BASE"),
        os.getenv("CREWAI_FRONTEND_API_KEY") or os.getenv("GEMINI_API_KEY"),
    )
    audit_llm = _build_llm(
        audit_model,
        os.getenv("CREWAI_AUDIT_API_BASE") or os.getenv("ANTHROPIC_API_BASE"),
        os.getenv("CREWAI_AUDIT_API_KEY") or os.getenv("ANTHROPIC_API_KEY"),
    )

    orchestrator = Agent(
        role=agents_cfg["orchestrator"]["role"],
        goal=agents_cfg["orchestrator"]["goal"],
        backstory=agents_cfg["orchestrator"]["backstory"],
        llm=planning_llm,
        allow_delegation=True,
        verbose=verbose,
    )
    backend_engineer_cfg = agents_cfg.get("backend_engineer") or agents_cfg.get("engineer")
    frontend_engineer_cfg = agents_cfg.get("frontend_engineer") or agents_cfg.get("engineer")
    backend_engineer = Agent(
        role=backend_engineer_cfg["role"],
        goal=backend_engineer_cfg["goal"],
        backstory=backend_engineer_cfg["backstory"],
        llm=backend_llm,
        allow_delegation=False,
        verbose=verbose,
    )
    frontend_engineer = Agent(
        role=frontend_engineer_cfg["role"],
        goal=frontend_engineer_cfg["goal"],
        backstory=frontend_engineer_cfg["backstory"],
        llm=frontend_llm,
        allow_delegation=False,
        verbose=verbose,
    )
    auditor = Agent(
        role=agents_cfg["auditor"]["role"],
        goal=agents_cfg["auditor"]["goal"],
        backstory=agents_cfg["auditor"]["backstory"],
        llm=audit_llm,
        allow_delegation=False,
        verbose=verbose,
    )

    diagnose_task = Task(
        description=tasks_cfg["diagnose"]["description"],
        expected_output=tasks_cfg["diagnose"]["expected_output"],
        agent=orchestrator,
    )
    backend_task_cfg = tasks_cfg.get("backend_execution") or tasks_cfg["planning"]
    frontend_task_cfg = tasks_cfg.get("frontend_execution") or tasks_cfg["planning"]
    backend_task = Task(
        description=backend_task_cfg["description"],
        expected_output=backend_task_cfg["expected_output"],
        agent=backend_engineer,
        context=[diagnose_task],
    )
    frontend_task = Task(
        description=frontend_task_cfg["description"],
        expected_output=frontend_task_cfg["expected_output"],
        agent=frontend_engineer,
        context=[diagnose_task, backend_task],
    )
    audit_task = Task(
        description=tasks_cfg["audit"]["description"],
        expected_output=tasks_cfg["audit"]["expected_output"],
        agent=auditor,
        context=[diagnose_task, backend_task, frontend_task],
    )

    return Crew(
        agents=[orchestrator, backend_engineer, frontend_engineer, auditor],
        tasks=[diagnose_task, backend_task, frontend_task, audit_task],
        process=Process.sequential,
        verbose=verbose,
    )
