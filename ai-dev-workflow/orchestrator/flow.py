from __future__ import annotations

import json
import os
import re
import subprocess
import hashlib
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
from pathlib import Path
from typing import Any

from dotenv import load_dotenv

from .cost_tracker import CostTracker
from .csv_bridge import CsvBridge, PhaseProgress
from .router import LLMRouter, RouterError, RouterResult
from .state import ProjectState
from .tasks import FlowTask, prompt_for_batch


def _now_tag() -> str:
    return datetime.now().strftime("%Y%m%d-%H%M%S")


def _state_file(repo_root: Path, flow_id: str) -> Path:
    return repo_root / ".crewai" / "flows" / f"{flow_id}.json"


def load_flow_state(repo_root: str | Path, flow_id: str) -> ProjectState:
    root = Path(repo_root).resolve()
    path = _state_file(root, flow_id)
    if not path.exists():
        raise FileNotFoundError(f"Flow state not found: {path}")
    payload = json.loads(path.read_text(encoding="utf-8"))
    if "state" in payload:
        return ProjectState.from_json_data(payload["state"])
    return ProjectState.from_json_data(payload)


def list_flow_states(repo_root: str | Path) -> list[ProjectState]:
    root = Path(repo_root).resolve()
    flow_dir = root / ".crewai" / "flows"
    if not flow_dir.exists():
        return []
    states: list[ProjectState] = []
    for path in sorted(flow_dir.glob("*.json")):
        try:
            payload = json.loads(path.read_text(encoding="utf-8"))
            state_data = payload["state"] if isinstance(payload, dict) and "state" in payload else payload
            states.append(ProjectState.from_json_data(state_data))
        except Exception:
            continue
    states.sort(key=lambda s: s.updated_at, reverse=True)
    return states


class EnterpriseDevFlow:
    """
    Lightweight execution flow that mirrors the framework-plan contract:
    - persistent state in `.crewai/flows/<flow-id>.json`
    - start/resume/status script support
    - stage 0-3 / 4 / 5 / 6 checkpoints
    """

    def __init__(
        self,
        repo_root: str | Path,
        mode: str | None = None,
        flow_id: str | None = None,
        max_tasks: int = 8,
        dry_run: bool = False,
        target_phase: int | None = None,
    ) -> None:
        self.repo_root = Path(repo_root).resolve()
        # Load workflow env defaults for script-driven runs.
        load_dotenv(self.repo_root / "ai-dev-workflow" / "config" / ".env", override=False)
        load_dotenv(self.repo_root / "ai-dev-workflow" / "crewai" / ".env", override=False)
        self.docs_dir = self.repo_root / "docs" / "crewai"
        self.flow_dir = self.repo_root / ".crewai" / "flows"
        self.docs_dir.mkdir(parents=True, exist_ok=True)
        self.flow_dir.mkdir(parents=True, exist_ok=True)

        self.max_tasks = max(1, max_tasks)
        self.dry_run = dry_run
        self.target_phase = target_phase if target_phase and target_phase > 0 else None
        self.bridge = CsvBridge(self.repo_root, target_phase=self.target_phase)

        if flow_id:
            self.state = load_flow_state(self.repo_root, flow_id)
        else:
            self.state = ProjectState(mode=(mode or "hybrid"))
        if mode:
            self.state.mode = mode

        self.cost_tracker = self._load_cost_tracker(self.state.flow_id)
        self.router = LLMRouter(
            mode=self.state.mode,
            dry_run=self.dry_run,
            cost_tracker=self.cost_tracker,
            working_dir=self.repo_root,
        )

    @property
    def flow_id(self) -> str:
        return self.state.flow_id

    @property
    def state_path(self) -> Path:
        return _state_file(self.repo_root, self.flow_id)

    def _load_cost_tracker(self, flow_id: str) -> CostTracker:
        path = _state_file(self.repo_root, flow_id)
        if not path.exists():
            return CostTracker()
        try:
            payload = json.loads(path.read_text(encoding="utf-8"))
            return CostTracker.from_payload(payload.get("cost_tracker"))
        except Exception:
            return CostTracker()

    def save_state(self) -> None:
        self.state.api_cost_usd = round(self.cost_tracker.total_cost_usd, 8)
        self.state.touch()
        payload = {
            "state": self.state.model_dump(),
            "cost_tracker": self.cost_tracker.model_dump(),
        }
        self.state_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")

    def _sync_phase_progress(self) -> PhaseProgress:
        progress = self.bridge.phase_progress()
        self.state.total_phases = progress.total_phases
        self.state.current_phase = progress.current_phase
        if progress.recommended_stage == 3:
            # Phase todo is empty -> Stage 3 decomposition is required.
            self.state.current_stage = 3
        elif progress.recommended_stage == 4:
            # Fresh flows should jump straight into Stage 4 when the phase
            # already has a todo list with unfinished execution work.
            self.state.current_stage = 4
        return progress

    def _append_process_log(self, message: str, phase_index: int | None = None) -> None:
        if self.dry_run:
            return
        self.bridge.append_process_log(message, phase_index=phase_index)

    def _append_phase_process_log(self, phase_index: int, message: str) -> None:
        if self.dry_run:
            return
        self.bridge.append_phase_process_log(phase_index, message)

    def kickoff(self, inputs: dict[str, Any] | None = None) -> dict[str, Any]:
        if inputs and inputs.get("id") and inputs["id"] != self.flow_id:
            self.state = load_flow_state(self.repo_root, str(inputs["id"]))
            self.cost_tracker = self._load_cost_tracker(self.state.flow_id)
            self.router = LLMRouter(
                mode=self.state.mode,
                dry_run=self.dry_run,
                cost_tracker=self.cost_tracker,
                working_dir=self.repo_root,
            )

        self.save_state()
        try:
            progress = self._sync_phase_progress()
            if progress.needs_decomposition:
                self.state.mark_stage(3)
                self.state.last_error = ""
                self._append_process_log(
                    f"flow={self.flow_id} stage=3 required "
                    f"current_phase={self.state.current_phase}/{max(self.state.total_phases, 1)} "
                    "reason=phase_todolist_empty"
                )
                self.save_state()
                return self.summary()
            if self.target_phase is not None and progress.current_phase > self.target_phase:
                self.state.mark_stage(4)
                self.state.last_error = ""
                self._append_process_log(
                    f"flow={self.flow_id} target_phase={self.target_phase} reached; stop at current_phase={progress.current_phase}"
                )
                self.save_state()
                return self.summary()
            if self.state.current_stage < 3:
                self.stage_0_3_planning()
            progress = self._sync_phase_progress()
            if not progress.stage4_complete:
                self.stage_4_execution()
                progress = self._sync_phase_progress()
            if self.target_phase is not None and progress.current_phase > self.target_phase:
                self.state.mark_stage(4)
                self.state.last_error = ""
                self._append_process_log(
                    f"flow={self.flow_id} target_phase={self.target_phase} reached after stage4; stop."
                )
                self.save_state()
                return self.summary()
            if not progress.stage4_complete:
                self.state.mark_stage(4)
                self.state.last_error = ""
                self._append_process_log(
                    f"flow={self.flow_id} stage=4 in-progress "
                    f"current_phase={self.state.current_phase}/{max(self.state.total_phases, 1)}"
                )
                self.save_state()
                return self.summary()
            if self.state.current_stage < 5:
                self.stage_5_review()
            if self.state.current_stage < 6:
                self.stage_6_deployment_ready()
            self.state.last_error = ""
            self.save_state()
            return self.summary()
        except Exception as exc:
            self.state.last_error = str(exc)
            self.save_state()
            self._append_process_log(f"crewai_unavailable:{exc}")
            raise

    def stage_0_3_planning(self) -> None:
        snapshot = self.bridge.render_snapshot(max_tasks=self.max_tasks)
        prompt = (
            "You are the workflow orchestrator. Analyze the current project snapshot and produce:\n"
            "1) stage confidence\n2) top 3 bottlenecks\n3) next 5-10 tasks in dependency order.\n\n"
            f"{snapshot}"
        )
        result = self.router.call("planning", prompt)
        out = self._write_doc("planning", result.text)
        self.state.mark_stage(3)
        self.state.add_output(str(out))
        self._append_process_log(
            f"flow={self.flow_id} stage=0-3 done provider={result.provider} model={result.model}"
        )
        self.save_state()

    def _split_execution_batches(self, tasks: list) -> tuple[list, list]:
        backend_batch = []
        frontend_batch = []
        for task in tasks:
            assigned = (task.assigned_agent or "").strip().lower()
            area = (task.area or "").strip().lower()
            task_type = (task.task_type or "").strip().lower()
            if assigned == "gemini" or area == "frontend" or task_type == "frontend":
                frontend_batch.append(task)
            else:
                backend_batch.append(task)
        return backend_batch, frontend_batch

    def _parse_task_state_hints(self, text: str, allowed_ids: set[str]) -> dict[str, str]:
        updates: dict[str, str] = {}
        priority = {"in_progress": 1, "blocked": 2, "done": 3}
        pattern = re.compile(r"(?im)^\s*(DONE|IN_PROGRESS|IN-PROGRESS|FAILED|BLOCKED)\s*:\s*([A-Za-z0-9_-]+)\s*$")
        for match in pattern.finditer(text or ""):
            raw_state = match.group(1).upper().replace("-", "_")
            task_id = match.group(2).strip()
            if task_id not in allowed_ids:
                continue
            if raw_state == "DONE":
                next_state = "done"
            elif raw_state in {"FAILED", "BLOCKED"}:
                next_state = "blocked"
            else:
                next_state = "in_progress"
            old_state = updates.get(task_id)
            if old_state is None or priority[next_state] >= priority.get(old_state, 0):
                updates[task_id] = next_state
        return updates

    def _truncate_for_review(self, text: str, max_chars: int = 4000) -> str:
        clean = (text or "").strip()
        if len(clean) <= max_chars:
            return clean
        keep = max(800, max_chars - 200)
        return f"{clean[:keep]}\n\n...[TRUNCATED {len(clean) - keep} chars]..."

    def _is_material_change_path(self, path: str) -> bool:
        normalized = (path or "").replace("\\", "/").strip()
        if not normalized:
            return False
        bookkeeping_prefixes = (
            ".crewai/",
            "docs/crewai/",
            "phases/",
            "memory/",
            ".tmp-cli-tests/",
        )
        bookkeeping_files = {
            "task_plan.md",
            "findings.md",
            "progress.md",
        }
        if normalized in bookkeeping_files:
            return False
        return not any(normalized.startswith(prefix) for prefix in bookkeeping_prefixes)

    def _material_change_fingerprints(self) -> dict[str, str]:
        try:
            proc = subprocess.run(
                ["git", "status", "--porcelain"],
                cwd=self.repo_root,
                capture_output=True,
                text=True,
                encoding="utf-8",
                errors="replace",
                timeout=30,
                check=False,
            )
        except Exception:
            return {}
        paths: dict[str, str] = {}
        for line in (proc.stdout or "").splitlines():
            if len(line) < 4:
                continue
            path = line[3:].strip()
            if " -> " in path:
                path = path.split(" -> ", 1)[1].strip()
            normalized = path.replace("\\", "/")
            if not self._is_material_change_path(normalized):
                continue
            abs_path = self.repo_root / normalized
            if abs_path.is_file():
                try:
                    digest = hashlib.sha1(abs_path.read_bytes()).hexdigest()
                except Exception:
                    digest = "read-error"
            elif abs_path.exists():
                digest = "non-file"
            else:
                digest = "missing"
            paths[normalized] = digest
        return paths

    def _call_role_safe(self, role: str, prompt: str) -> tuple[RouterResult, str | None]:
        try:
            return self.router.call(role, prompt), None
        except RouterError as exc:
            return (
                RouterResult(
                    text=f"EXECUTION_UNAVAILABLE: {exc}",
                    provider="error",
                    model=f"{role}-unavailable",
                ),
                str(exc),
            )

    def _validation_timeout_seconds(self) -> int:
        raw = os.getenv("VALIDATION_TIMEOUT_SECONDS")
        try:
            return max(60, int(raw or "900"))
        except ValueError:
            return 900

    def _execution_review_interval(self) -> int:
        raw = os.getenv("EXECUTION_REVIEW_INTERVAL", "1")
        try:
            return max(0, int(raw))
        except ValueError:
            return 1

    def _run_validation_command(self, command: str) -> tuple[bool, str]:
        cmd = (command or "").strip()
        if not cmd:
            return False, "missing_val_command"
        try:
            proc = subprocess.run(
                cmd,
                cwd=self.repo_root,
                shell=True,
                capture_output=True,
                text=True,
                encoding="utf-8",
                errors="replace",
                timeout=self._validation_timeout_seconds(),
                check=False,
            )
        except subprocess.TimeoutExpired:
            return False, "validation_timeout"
        output = ((proc.stdout or "") + "\n" + (proc.stderr or "")).strip()
        if len(output) > 800:
            output = output[:800] + "...[truncated]"
        if proc.returncode == 0:
            return True, output or "validation_ok"
        return False, output or f"validation_exit={proc.returncode}"

    def _verified_updates(
        self,
        tasks: list[FlowTask],
        raw_updates: dict[str, str],
        new_material_changes: set[str],
        execution_error: str | None,
    ) -> tuple[dict[str, str], list[str]]:
        task_map = {task.task_id: task for task in tasks if task.task_id}
        if not task_map:
            return {}, []

        notes: list[str] = []
        if execution_error:
            blocked = {task_id: "blocked" for task_id in task_map}
            notes.extend(f"{task_id}: execution_unavailable={execution_error}" for task_id in task_map)
            return blocked, notes

        updates = dict(raw_updates)
        if not updates:
            updates = {task_id: "in_progress" for task_id in task_map}

        verified: dict[str, str] = {}
        has_material_changes = bool(new_material_changes)
        for task_id, state in updates.items():
            task = task_map.get(task_id)
            if not task:
                continue
            if state != "done":
                verified[task_id] = state
                continue
            if not has_material_changes:
                verified[task_id] = "in_progress"
                notes.append(f"{task_id}: skipped_done_without_material_diff")
                continue
            ok, detail = self._run_validation_command(task.val_command)
            if ok:
                verified[task_id] = "done"
                notes.append(f"{task_id}: validation_ok")
            else:
                verified[task_id] = "in_progress"
                notes.append(f"{task_id}: validation_failed={detail}")
        return verified, notes

    def stage_4_execution(self) -> None:
        progress = self.bridge.phase_progress()
        phase_index = progress.current_phase
        phase_csv = self.bridge.phase_csv_path(phase_index)
        ready_tasks = self.bridge.to_flow_tasks(self.bridge.phase_ready_rows(phase_index)[: self.max_tasks])
        backend_tasks, frontend_tasks = self._split_execution_batches(ready_tasks)

        backend_prompt = prompt_for_batch(
            backend_tasks,
            f"Execute backend coding tasks for Stage 4, Phase {phase_index}.",
        )
        backend_prompt += (
            "\n\nReturn markdown with changed files, commands executed, and validation results.\n"
            "For each task you started/completed, append machine-readable lines:\n"
            "IN_PROGRESS:<task_id>\nDONE:<task_id>\nFAILED:<task_id>\n"
            "Only use FAILED when there is a hard blocker (e.g., unmet dependency, missing API contract, or impossible requirement).\n"
            "Mark DONE only after workspace files are actually written and the task val_command succeeds in this environment.\n"
            "If code is drafted but validation cannot run or fails, mark IN_PROGRESS and explain the blocker in text.\n"
            "Do not mark DONE for plan-only, analysis-only, or doc-only output.\n"
        )
        backend_ids = [task.task_id for task in backend_tasks if task.task_id]
        if backend_ids:
            backend_prompt += (
                "\nOnly use task IDs from this exact list, and emit at least one status line per listed task:\n"
                + ", ".join(backend_ids)
                + "\nDo not emit any unrelated task ID."
            )
        if phase_csv:
            backend_prompt += f"\nFocus file: {phase_csv}"

        frontend_source = frontend_tasks
        frontend_prompt = prompt_for_batch(
            frontend_source,
            f"Execute frontend coding tasks for Stage 4, Phase {phase_index}.",
        )
        frontend_prompt += (
            "\n\nIf no dependency-ready task exists, return a short unblock checklist for next frontend task.\n"
            "When you start/complete tasks, append machine-readable lines:\n"
            "IN_PROGRESS:<task_id>\nDONE:<task_id>\nFAILED:<task_id>\n"
            "Only use FAILED when there is a hard blocker (e.g., unmet dependency or impossible requirement).\n"
            "Mark DONE only after workspace files are actually written and the task val_command succeeds in this environment.\n"
            "If code is drafted but validation cannot run or fails, mark IN_PROGRESS and explain the blocker in text.\n"
            "Do not mark DONE for plan-only, analysis-only, or doc-only output.\n"
        )
        frontend_ids = [task.task_id for task in frontend_source if task.task_id]
        if frontend_ids:
            frontend_prompt += (
                "\nOnly use task IDs from this exact list, and emit at least one status line per listed task:\n"
                + ", ".join(frontend_ids)
                + "\nDo not emit any unrelated task ID."
            )
        if phase_csv:
            frontend_prompt += f"\nFocus file: {phase_csv}"

        pre_material_changes = self._material_change_fingerprints()

        backend_result = RouterResult(text="No backend task selected in this round.", provider="skip", model="skip-no-backend")
        backend_error: str | None = None
        frontend_result = RouterResult(text="No frontend task selected in this round.", provider="skip", model="skip-no-frontend")
        frontend_error: str | None = None

        with ThreadPoolExecutor(max_workers=2) as executor:
            futures = {}
            if backend_tasks:
                futures["backend"] = executor.submit(self._call_role_safe, "backend", backend_prompt)
            if frontend_source:
                futures["frontend"] = executor.submit(self._call_role_safe, "frontend", frontend_prompt)
            if "backend" in futures:
                backend_result, backend_error = futures["backend"].result()
            if "frontend" in futures:
                frontend_result, frontend_error = futures["frontend"].result()

        post_material_changes = self._material_change_fingerprints()
        new_material_changes = {
            path
            for path, fingerprint in post_material_changes.items()
            if pre_material_changes.get(path) != fingerprint
        }

        backend_updates = self._parse_task_state_hints(backend_result.text, set(backend_ids))
        frontend_updates = self._parse_task_state_hints(frontend_result.text, set(frontend_ids))
        backend_updates, backend_notes = self._verified_updates(
            backend_tasks,
            backend_updates,
            new_material_changes,
            backend_error,
        )
        frontend_updates, frontend_notes = self._verified_updates(
            frontend_source,
            frontend_updates,
            new_material_changes,
            frontend_error,
        )
        updates = {}
        updates.update(backend_updates)
        updates.update(frontend_updates)

        review_interval = self._execution_review_interval()
        review_round = self.state.bump_stage4_round()
        should_review = review_interval > 0 and (
            review_round % review_interval == 0 or any(state == "blocked" for state in updates.values())
        )
        if should_review:
            backend_for_review = self._truncate_for_review(backend_result.text, max_chars=4500)
            frontend_for_review = self._truncate_for_review(frontend_result.text, max_chars=4500)
            review_prompt = (
                f"Review Stage 4 execution outputs for Phase {phase_index} and return blocker-focused audit.\n"
                "Return exact line `BLOCKERS=NONE` if no blocker.\n\n"
                "## Backend Output\n"
                f"{backend_for_review}\n\n"
                "## Frontend Output\n"
                f"{frontend_for_review}\n"
            )
            if phase_csv:
                review_prompt += f"\nFocus file: {phase_csv}"
            try:
                review_result = self.router.call("review", review_prompt)
            except RouterError as exc:
                review_result = RouterResult(
                    text=f"REVIEW_UNAVAILABLE: {exc}\nBLOCKERS=UNKNOWN",
                    provider="fallback",
                    model="review-unavailable",
                )
        else:
            review_result = RouterResult(
                text=(
                    f"SKIPPED_BY_CONFIG: EXECUTION_REVIEW_INTERVAL={review_interval}, "
                    f"stage4_round={review_round}"
                ),
                provider="skip",
                model="skip-review-interval",
            )

        changed = self.bridge.apply_phase_task_updates(
            phase_index=phase_index,
            updates=updates,
            note=f"started_by=crewai flow={self.flow_id}",
        )
        for task_id, state in updates.items():
            if state == "done":
                self.state.record_task(task_id, success=True)
            elif state == "blocked":
                self.state.record_task(task_id, success=False)

        doc_lines = [
            f"# Stage 4 Execution Dispatch (Phase {phase_index})",
            "",
            f"- flow_id: {self.flow_id}",
            f"- phase_csv: {phase_csv}",
            f"- csv_updates: {changed}",
            "",
            f"## Backend Agent ({backend_result.provider} / {backend_result.model})",
            backend_result.text,
            "",
            f"## Frontend Agent ({frontend_result.provider} / {frontend_result.model})",
            frontend_result.text,
            "",
            f"## Audit Agent ({review_result.provider} / {review_result.model})",
            review_result.text,
            "",
            "## Execution Verification",
            f"- material_changes_before: {len(pre_material_changes)}",
            f"- material_changes_after: {len(post_material_changes)}",
            f"- new_material_changes: {len(new_material_changes)}",
        ]
        if new_material_changes:
            doc_lines.extend(["", "### New Material Changes", *[f"- {path}" for path in sorted(new_material_changes)]])
        if backend_notes or frontend_notes:
            doc_lines.extend(
                [
                    "",
                    "### Verification Notes",
                    *[f"- {note}" for note in backend_notes + frontend_notes],
                ]
            )
        out = self._write_doc("execution", "\n".join(doc_lines))
        self.state.mark_stage(4)
        self.state.add_output(str(out))
        self._append_process_log(
            "flow="
            f"{self.flow_id} stage=4 phase={phase_index} "
            f"backend={backend_result.model} frontend={frontend_result.model} review={review_result.model} "
            f"updated={changed}"
        )
        self._append_phase_process_log(
            phase_index,
            "stage=4 execution started "
            f"backend={backend_result.model} frontend={frontend_result.model} review={review_result.model} "
            f"updated={changed}",
        )
        self.bridge.refresh_phase_process_metrics(phase_index)
        self.save_state()

    def stage_5_review(self) -> None:
        phase_index = self.bridge.focus_phase_index()
        if phase_index:
            open_rows = self.bridge.phase_open_rows(phase_index)[: self.max_tasks]
        else:
            open_rows = self.bridge.open_rows()[: self.max_tasks]
        snapshot = self.bridge.render_snapshot(max_tasks=self.max_tasks)
        prompt = (
            "Review current increment and produce blocker-focused audit output.\n"
            "Return exact line `BLOCKERS=NONE` if no blocking issues.\n\n"
            f"{snapshot}\n\nOpen rows count: {len(open_rows)}"
        )
        result = self.router.call("review", prompt)
        out = self._write_doc("review", result.text)
        self.state.mark_stage(5)
        self.state.add_output(str(out))
        self._append_process_log(
            f"flow={self.flow_id} stage=5 reviewed provider={result.provider} model={result.model}"
        )
        self.save_state()

    def stage_6_deployment_ready(self) -> None:
        summary = [
            f"# Deployment Ready Summary ({self.flow_id})",
            "",
            f"- current_stage: {self.state.current_stage}",
            f"- current_phase: {self.state.current_phase}",
            f"- api_cost_usd: {self.state.api_cost_usd}",
            f"- outputs_count: {len(self.state.outputs)}",
            "",
            "## Next Checks",
            "1. Validate Stage 5 blockers are closed.",
            "2. Ensure docs/api-contracts and DB migration are synced.",
            "3. Run regression and update process.md handoff notes.",
        ]
        out = self._write_doc("deployment-ready", "\n".join(summary))
        self.state.mark_stage(6)
        self.state.add_output(str(out))
        self._append_process_log(f"flow={self.flow_id} stage=6 summary generated")
        self.save_state()

    def _write_doc(self, kind: str, content: str) -> Path:
        name = f"{_now_tag()}-{self.flow_id[:8]}-{kind}.md"
        path = self.docs_dir / name
        path.write_text(content, encoding="utf-8")
        return path

    def summary(self) -> dict[str, Any]:
        return {
            "flow_id": self.flow_id,
            "state_path": str(self.state_path),
            "current_stage": self.state.current_stage,
            "current_phase": self.state.current_phase,
            "total_phases": self.state.total_phases,
            "target_phase": self.target_phase,
            "api_cost_usd": self.state.api_cost_usd,
            "outputs": self.state.outputs,
            "last_error": self.state.last_error,
        }
