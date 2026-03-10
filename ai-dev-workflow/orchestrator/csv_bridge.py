from __future__ import annotations

import csv
import re
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Iterable

from .tasks import FlowTask


DONE_STATES = {
    "done",
    "completed",
    "\u5df2\u5b8c\u6210",
    "\u5b8c\u6210",
}


def _is_done_state(value: str) -> bool:
    return (value or "").strip().lower() in DONE_STATES


@dataclass(frozen=True)
class PhaseStat:
    phase_index: int
    phase_name: str
    csv_path: Path
    total_tasks: int
    done_tasks: int
    open_tasks: int


@dataclass(frozen=True)
class PhaseProgress:
    total_phases: int
    current_phase: int
    stage4_complete: bool
    recommended_stage: int
    needs_decomposition: bool
    stats: list[PhaseStat]


@dataclass
class CsvBridge:
    repo_root: Path
    todo_rel: str = "todolist.csv"
    process_rel: str = "process.md"
    target_phase: int | None = None
    write_root_state: bool = False

    @property
    def todo_path(self) -> Path:
        return self.repo_root / self.todo_rel

    @property
    def process_path(self) -> Path:
        return self.repo_root / self.process_rel

    def _load_rows_from_csv(self, csv_path: Path) -> list[dict[str, str]]:
        if not csv_path.exists():
            return []
        with csv_path.open("r", encoding="utf-8-sig", newline="") as f:
            return list(csv.DictReader(f))

    def load_rows(self) -> list[dict[str, str]]:
        return self._load_rows_from_csv(self.todo_path)

    def open_rows(self) -> list[dict[str, str]]:
        rows = self.load_rows()
        return [row for row in rows if not _is_done_state(row.get("dev_state", ""))]

    def _split_dependencies(self, raw: str) -> list[str]:
        text = (raw or "").strip()
        if not text or text in {"-", "none", "None"}:
            return []
        # Keep token ranges as one dependency token (e.g. S0-001..S0-007)
        items = [x.strip() for x in re.split(r"[,;，、]", text) if x.strip()]
        return items

    def _ready_rows_from_rows(self, rows: list[dict[str, str]]) -> list[dict[str, str]]:
        done_ids = {row.get("id", "") for row in rows if _is_done_state(row.get("dev_state", ""))}
        existing_ids = {row.get("id", "") for row in rows}
        ready: list[dict[str, str]] = []
        for row in rows:
            if _is_done_state(row.get("dev_state", "")):
                continue
            deps = self._split_dependencies(row.get("dependencies", ""))
            unresolved = []
            for dep in deps:
                # Range expression or unknown IDs are treated as external/already handled.
                if ".." in dep or dep not in existing_ids:
                    continue
                if dep not in done_ids:
                    unresolved.append(dep)
            if not unresolved:
                ready.append(row)
        return ready

    def ready_rows(self) -> list[dict[str, str]]:
        return self._ready_rows_from_rows(self.load_rows())

    def to_flow_tasks(self, rows: Iterable[dict[str, str]]) -> list[FlowTask]:
        tasks: list[FlowTask] = []
        for row in rows:
            tasks.append(
                FlowTask(
                    task_id=row.get("id", ""),
                    title=row.get("title", ""),
                    area=row.get("area", ""),
                    task_type=row.get("task_type", ""),
                    description=row.get("description", ""),
                    acceptance_criteria=row.get("acceptance_criteria", ""),
                    dependencies=row.get("dependencies", ""),
                    val_command=row.get("val_command", ""),
                    assigned_agent=row.get("assigned_agent", ""),
                    refs=row.get("refs", ""),
                )
            )
        return tasks

    def render_snapshot(self, max_tasks: int = 8) -> str:
        phase_index = self.focus_phase_index()
        if phase_index:
            return self.render_phase_snapshot(phase_index, max_tasks=max_tasks)

        process_excerpt = ""
        if self.process_path.exists():
            process_excerpt = "\n".join(
                self.process_path.read_text(encoding="utf-8", errors="ignore").splitlines()[:120]
            )
        open_rows = self.open_rows()[:max_tasks]
        lines = [
            f"process_file: {self.process_path}",
            f"todo_file: {self.todo_path}",
            "",
            "process_excerpt:",
            process_excerpt or "(empty)",
            "",
            "open_tasks:",
        ]
        if not open_rows:
            lines.append("- none")
        else:
            for row in open_rows:
                lines.append(
                    f"- [{row.get('id','?')}] phase={row.get('phase','?')} area={row.get('area','?')} "
                    f"type={row.get('task_type','?')} title={row.get('title','?')} "
                    f"deps={row.get('dependencies','-')} dev_state={row.get('dev_state','?')}"
                )
        return "\n".join(lines)

    def focus_phase_index(self) -> int | None:
        if self.target_phase:
            if self.phase_csv_path(self.target_phase) or self.phase_process_path(self.target_phase):
                return self.target_phase
        progress = self.phase_progress()
        if progress.total_phases > 0:
            return progress.current_phase
        return None

    def render_phase_snapshot(self, phase_index: int, max_tasks: int = 8) -> str:
        process_path = self.phase_process_path(phase_index)
        csv_path = self.phase_csv_path(phase_index)
        process_excerpt = ""
        if process_path and process_path.exists():
            process_excerpt = "\n".join(
                process_path.read_text(encoding="utf-8", errors="ignore").splitlines()[:120]
            )
        rows = self.phase_rows(phase_index)
        open_rows = [row for row in rows if not _is_done_state(row.get("dev_state", ""))][:max_tasks]
        lines = [
            f"phase_scope: {phase_index}",
            f"process_file: {process_path or '(missing)'}",
            f"todo_file: {csv_path or '(missing)'}",
            "",
            "process_excerpt:",
            process_excerpt or "(empty)",
            "",
            "open_tasks:",
        ]
        if not open_rows:
            lines.append("- none")
        else:
            for row in open_rows:
                lines.append(
                    f"- [{row.get('id','?')}] phase={row.get('phase','?')} area={row.get('area','?')} "
                    f"type={row.get('task_type','?')} title={row.get('title','?')} "
                    f"deps={row.get('dependencies','-')} dev_state={row.get('dev_state','?')}"
                )
        return "\n".join(lines)

    def _phase_dirs(self) -> list[Path]:
        phases_root = self.repo_root / "phases"
        if not phases_root.exists():
            return []
        dirs = [p for p in phases_root.glob("phase-*") if p.is_dir()]

        def _sort_key(path: Path) -> tuple[int, str]:
            match = re.search(r"phase-(\d+)", path.name.lower())
            if match:
                return (int(match.group(1)), path.name)
            return (10_000, path.name)

        return sorted(dirs, key=_sort_key)

    def phase_csv_path(self, phase_index: int) -> Path | None:
        for phase_dir in self._phase_dirs():
            if self._phase_index(phase_dir.name, 0) == phase_index:
                return phase_dir / "todolist.csv"
        return None

    def phase_rows(self, phase_index: int) -> list[dict[str, str]]:
        csv_path = self.phase_csv_path(phase_index)
        if not csv_path:
            return []
        return self._load_rows_from_csv(csv_path)

    def phase_ready_rows(self, phase_index: int) -> list[dict[str, str]]:
        return self._ready_rows_from_rows(self.phase_rows(phase_index))

    def phase_open_rows(self, phase_index: int) -> list[dict[str, str]]:
        return [row for row in self.phase_rows(phase_index) if not _is_done_state(row.get("dev_state", ""))]

    def _phase_index(self, phase_name: str, fallback: int) -> int:
        match = re.search(r"phase-(\d+)", phase_name.lower())
        if match:
            return int(match.group(1))
        return fallback

    def phase_process_path(self, phase_index: int) -> Path | None:
        for phase_dir in self._phase_dirs():
            if self._phase_index(phase_dir.name, 0) == phase_index:
                return phase_dir / "process.md"
        return None

    def _write_rows_to_csv(
        self,
        csv_path: Path,
        rows: list[dict[str, str]],
        fieldnames: list[str] | None,
    ) -> None:
        names = fieldnames or []
        if not names and rows:
            names = list(rows[0].keys())
        csv_path.parent.mkdir(parents=True, exist_ok=True)
        with csv_path.open("w", encoding="utf-8-sig", newline="") as f:
            writer = csv.DictWriter(f, fieldnames=names)
            writer.writeheader()
            writer.writerows(rows)

    def apply_phase_task_updates(
        self,
        phase_index: int,
        updates: dict[str, str],
        note: str = "",
    ) -> int:
        csv_path = self.phase_csv_path(phase_index)
        if not csv_path or not csv_path.exists() or not updates:
            return 0

        with csv_path.open("r", encoding="utf-8-sig", newline="") as f:
            reader = csv.DictReader(f)
            rows = list(reader)
            fieldnames = reader.fieldnames

        def _normalize_state(value: str) -> str:
            text = (value or "").strip().lower()
            if text in {"in_progress", "in-progress", "started"}:
                return "in_progress"
            if text in {"done", "completed", "complete", "finished"}:
                return "done"
            if text in {"blocked", "failed", "error"}:
                return "blocked"
            if text in {"pending"}:
                return "pending"
            return text or "pending"

        changed = 0
        for row in rows:
            task_id = (row.get("id") or "").strip()
            if not task_id or task_id not in updates:
                continue
            old_state = (row.get("dev_state") or "").strip()
            new_state = _normalize_state(updates[task_id])
            # Keep completion monotonic: once a task is done, do not downgrade
            # to in_progress/blocked/pending from later noisy model outputs.
            if _is_done_state(old_state) and new_state != "done":
                if note:
                    old_note = (row.get("notes") or "").strip()
                    if note not in old_note:
                        row["notes"] = f"{old_note}; {note}".strip("; ").strip()
                continue
            if old_state != new_state:
                row["dev_state"] = new_state
                changed += 1
                attempts_raw = (row.get("attempts") or "0").strip()
                try:
                    attempts = int(attempts_raw)
                except ValueError:
                    attempts = 0
                row["attempts"] = str(max(attempts, 0) + 1)
            if note:
                old_note = (row.get("notes") or "").strip()
                if note not in old_note:
                    row["notes"] = f"{old_note}; {note}".strip("; ").strip()

        if changed:
            self._write_rows_to_csv(csv_path, rows, fieldnames)
        return changed

    def phase_progress(self) -> PhaseProgress:
        stats: list[PhaseStat] = []
        for idx, phase_dir in enumerate(self._phase_dirs(), start=1):
            csv_path = phase_dir / "todolist.csv"
            rows = self._load_rows_from_csv(csv_path)
            total = len(rows)
            done = sum(1 for row in rows if _is_done_state(row.get("dev_state", "")))
            open_tasks = max(total - done, 0)
            phase_index = self._phase_index(phase_dir.name, idx)
            stats.append(
                PhaseStat(
                    phase_index=phase_index,
                    phase_name=phase_dir.name,
                    csv_path=csv_path,
                    total_tasks=total,
                    done_tasks=done,
                    open_tasks=open_tasks,
                )
            )

        if not stats:
            open_rows = self.open_rows()
            # Fallback for repositories that use only root todolist.csv.
            return PhaseProgress(
                total_phases=1 if open_rows else 0,
                current_phase=1,
                stage4_complete=(len(open_rows) == 0),
                recommended_stage=(4 if open_rows else 6),
                needs_decomposition=False,
                stats=[],
            )

        for item in stats:
            # A phase is complete only when its todo exists and all rows are done.
            if item.total_tasks == 0 or item.open_tasks > 0:
                return PhaseProgress(
                    total_phases=len(stats),
                    current_phase=item.phase_index,
                    stage4_complete=False,
                    recommended_stage=(3 if item.total_tasks == 0 else 4),
                    needs_decomposition=(item.total_tasks == 0),
                    stats=stats,
                )

        return PhaseProgress(
            total_phases=len(stats),
            current_phase=stats[-1].phase_index,
            stage4_complete=True,
            recommended_stage=6,
            needs_decomposition=False,
            stats=stats,
        )

    def _append_log_to_path(self, path: Path, title: str, message: str) -> None:
        stamp = datetime.now().strftime("%H:%M")
        line = f"{stamp} [crewai] {message}"
        if path.exists():
            text = path.read_text(encoding="utf-8", errors="ignore")
            newline = "\r\n" if "\r\n" in text else "\n"
        else:
            text = f"{title}\n\n## Execution Log\n\n```text\n```"
            newline = "\n"
        if "## Execution Log" in text and "```text" in text:
            updated = text.replace("```text", f"```text{newline}{line}", 1)
        else:
            updated = text + f"{newline}{line}{newline}"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(updated, encoding="utf-8")

    def append_process_log(self, message: str, phase_index: int | None = None) -> None:
        scoped_phase = phase_index or self.focus_phase_index()
        if scoped_phase:
            path = self.phase_process_path(scoped_phase)
            if path:
                self._append_log_to_path(path, "# Phase Progress", message)
                return
        if self.write_root_state:
            self._append_log_to_path(self.process_path, "# Workflow Progress", message)

    def append_phase_process_log(self, phase_index: int, message: str) -> None:
        path = self.phase_process_path(phase_index)
        if not path:
            return
        self._append_log_to_path(path, "# Phase Progress", message)

    def refresh_phase_process_metrics(self, phase_index: int) -> None:
        path = self.phase_process_path(phase_index)
        if not path or not path.exists():
            return
        rows = self.phase_rows(phase_index)
        total = len(rows)
        done = sum(1 for row in rows if _is_done_state(row.get("dev_state", "")))
        in_progress = sum(1 for row in rows if (row.get("dev_state", "").strip().lower() == "in_progress"))
        blocked = sum(1 for row in rows if (row.get("dev_state", "").strip().lower() == "blocked"))
        pending = max(total - done - in_progress - blocked, 0)
        rate = "0.00%"
        if total > 0:
            rate = f"{(done * 100.0 / total):.2f}%"

        open_rows = [row for row in rows if not _is_done_state(row.get("dev_state", ""))]
        focus = "none"
        if open_rows:
            first = open_rows[0]
            focus = f"{first.get('id', '?')} ({first.get('title', '').strip()})"

        text = path.read_text(encoding="utf-8", errors="ignore")
        replacements = {
            "total_tasks": str(total),
            "done": str(done),
            "in_progress": str(in_progress),
            "pending": str(pending),
            "blocked": str(blocked),
            "completion_rate": rate,
        }
        for key, value in replacements.items():
            text = re.sub(rf"\| {re.escape(key)} \| .*?\|", f"| {key} | {value} |", text)
        stage4_status = "Ready"
        if total > 0 and done >= total:
            stage4_status = "Completed"
        elif in_progress > 0:
            stage4_status = "In Progress"
        text = re.sub(
            r"\| Stage 4: Execution \| .*? \| .*?\|",
            f"| Stage 4: Execution | {stage4_status} | - |",
            text,
        )
        now_text = datetime.now().astimezone().isoformat(timespec="seconds")
        text = re.sub(r"^- Current task: .*?$", f"- Current task: {focus}", text, flags=re.MULTILINE)
        text = re.sub(r"^- Last update: .*?$", f"- Last update: {now_text}", text, flags=re.MULTILINE)
        path.write_text(text, encoding="utf-8")
