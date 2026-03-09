from __future__ import annotations

import argparse
import os
from datetime import datetime
from pathlib import Path

from dotenv import load_dotenv

from .context import build_workflow_snapshot
from .crew import build_workflow_crew


def _build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Run CrewAI against current workflow state.")
    parser.add_argument("--repo-root", default=".", help="Repository root path")
    parser.add_argument("--process", default="process.md", help="Relative path to process file")
    parser.add_argument("--todo", default="todolist.csv", help="Relative path to todo csv")
    parser.add_argument("--output", default="docs/crewai/next-actions.md", help="Output markdown path")
    parser.add_argument("--max-tasks", type=int, default=8, help="Max open tasks to include in context")
    parser.add_argument("--verbose", action="store_true", help="Enable verbose logs")
    return parser


def _normalize_base_url(value: str | None) -> str | None:
    text = (value or "").strip().rstrip("/")
    if not text:
        return None
    if text.endswith("/v1"):
        return text
    return f"{text}/v1"


def _select_crewai_provider() -> str:
    provider = (os.getenv("CREWAI_PROVIDER") or "openai").strip().lower()
    if provider in {"openai", "anthropic", "gemini"}:
        return provider
    return "openai"


def _apply_crewai_llm_env() -> None:
    provider = _select_crewai_provider()
    model = os.getenv("CREWAI_MODEL_NAME")
    base_url = os.getenv("CREWAI_API_BASE")
    api_key = os.getenv("CREWAI_API_KEY")

    if provider == "anthropic":
        model = model or os.getenv("MODEL_REVIEW") or "claude-opus-4-6"
        base_url = base_url or os.getenv("ANTHROPIC_API_BASE")
        api_key = api_key or os.getenv("ANTHROPIC_API_KEY")
    elif provider == "gemini":
        model = model or os.getenv("MODEL_FRONTEND") or "gemini-3-flash-preview"
        base_url = base_url or os.getenv("GEMINI_API_BASE")
        api_key = api_key or os.getenv("GEMINI_API_KEY")
    else:
        model = model or os.getenv("MODEL_PLANNING") or "gpt-5.2-codex"
        base_url = base_url or os.getenv("OPENAI_BASE_URL") or os.getenv("OPENAI_API_BASE")
        api_key = api_key or os.getenv("OPENAI_API_KEY")

    normalized_base = _normalize_base_url(base_url)
    if model:
        os.environ["OPENAI_MODEL_NAME"] = model
    if normalized_base:
        os.environ["OPENAI_BASE_URL"] = normalized_base
    if api_key:
        os.environ["OPENAI_API_KEY"] = api_key


def run() -> str:
    parser = _build_parser()
    args = parser.parse_args()

    repo_root = Path(args.repo_root).resolve()
    crewai_root = Path(__file__).resolve().parents[2]
    workflow_root = Path(__file__).resolve().parents[3]
    # Support both the minimal crewai-local env and framework-plan config env.
    load_dotenv(workflow_root / "config" / ".env", override=False)
    load_dotenv(crewai_root / ".env", override=False)
    _apply_crewai_llm_env()
    if (
        not os.getenv("OPENAI_API_KEY")
        and not os.getenv("ANTHROPIC_API_KEY")
        and not os.getenv("GEMINI_API_KEY")
    ):
        raise SystemExit(
            "Missing credentials: set OPENAI_API_KEY / ANTHROPIC_API_KEY / GEMINI_API_KEY in "
            "ai-dev-workflow/config/.env or ai-dev-workflow/crewai/.env."
        )

    snapshot = build_workflow_snapshot(
        repo_root=repo_root,
        process_rel=args.process,
        todo_rel=args.todo,
        max_tasks=args.max_tasks,
    )

    crew = build_workflow_crew(
        config_root=Path(__file__).resolve().parent / "config",
        verbose=args.verbose,
    )

    result = crew.kickoff(
        inputs={
            "workflow_snapshot": snapshot.to_prompt(),
            "current_time": datetime.now().isoformat(),
        }
    )

    output_path = Path(args.output)
    if not output_path.is_absolute():
        output_path = repo_root / output_path
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(str(result), encoding="utf-8")

    print(f"[CrewAI] Output written to: {output_path}")
    return str(output_path)


if __name__ == "__main__":
    run()
