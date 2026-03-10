from __future__ import annotations

import csv
from dataclasses import dataclass
from pathlib import Path


DONE_STATES = {"done", "completed"}


@dataclass
class WorkflowSnapshot:
    repo_root: Path
    process_path: Path
    todo_path: Path
    process_excerpt: str
    open_tasks: list[dict[str, str]]

    def to_prompt(self) -> str:
        lines = [
            f"repo_root: {self.repo_root}",
            f"process_file: {self.process_path}",
            f"todo_file: {self.todo_path}",
            "",
            "process_excerpt:",
            self.process_excerpt.strip() or "(empty)",
            "",
            "open_tasks:",
        ]
        if not self.open_tasks:
            lines.append("- none")
        else:
            for row in self.open_tasks:
                lines.append(
                    f"- [{row.get('id','?')}] phase={row.get('phase','?')} "
                    f"area={row.get('area','?')} task_type={row.get('task_type','?')} "
                    f"title={row.get('title','?')} deps={row.get('dependencies','-')} "
                    f"dev_state={row.get('dev_state','?')} assigned={row.get('assigned_agent','?')}"
                )
        return "\n".join(lines)


def _read_text_excerpt(path: Path, max_lines: int = 120) -> str:
    if not path.exists():
        return ""
    content = path.read_text(encoding="utf-8", errors="ignore").splitlines()
    return "\n".join(content[:max_lines])


def _is_open_state(value: str) -> bool:
    normalized = (value or "").strip().lower()
    return normalized not in DONE_STATES


def _load_open_tasks(path: Path, max_tasks: int) -> list[dict[str, str]]:
    if not path.exists():
        return []
    with path.open("r", encoding="utf-8-sig", newline="") as file:
        rows = list(csv.DictReader(file))
    open_rows = [row for row in rows if _is_open_state(row.get("dev_state", ""))]
    return open_rows[:max_tasks]


def build_workflow_snapshot(
    repo_root: Path,
    process_rel: str = "process.md",
    todo_rel: str = "todolist.csv",
    max_tasks: int = 8,
) -> WorkflowSnapshot:
    process_path = repo_root / process_rel
    todo_path = repo_root / todo_rel
    process_excerpt = _read_text_excerpt(process_path)
    open_tasks = _load_open_tasks(todo_path, max_tasks=max_tasks)
    return WorkflowSnapshot(
        repo_root=repo_root,
        process_path=process_path,
        todo_path=todo_path,
        process_excerpt=process_excerpt,
        open_tasks=open_tasks,
    )
