from __future__ import annotations

import json
import os
import shutil
import subprocess
import time
import urllib.error
import urllib.request
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from .cost_tracker import CostTracker


class RouterError(RuntimeError):
    pass


@dataclass
class RouterResult:
    text: str
    provider: str
    model: str


class LLMRouter:
    """Dual-mode router: local CLI first, relay API fallback."""

    def __init__(
        self,
        mode: str | None = None,
        timeout_seconds: int = 120,
        dry_run: bool = False,
        cost_tracker: CostTracker | None = None,
        working_dir: str | Path | None = None,
    ) -> None:
        self.mode = (mode or os.getenv("LLM_MODE", "hybrid")).strip().lower()
        self.timeout_seconds = timeout_seconds
        self.dry_run = dry_run
        self.cost_tracker = cost_tracker or CostTracker()
        self._cli_failures = 0
        self.working_dir = str(Path(working_dir).resolve()) if working_dir else None
        self._cli_health_cache: dict[str, bool] = {}

    def call(self, role: str, prompt: str) -> RouterResult:
        role = role.strip().lower()
        if self.dry_run:
            return RouterResult(
                text=f"[DRY-RUN] role={role}\n{prompt[:500]}",
                provider="dry-run",
                model="dry-run-model",
            )
        if self._execution_cli_only(role):
            try:
                return self._call_cli(role, prompt)
            except RouterError:
                if self._execution_api_fallback_enabled(role):
                    return self._call_api(role, prompt)
                raise
        if self.mode == "cli":
            return self._call_cli(role, prompt)
        if self.mode == "api":
            try:
                return self._call_api(role, prompt)
            except RouterError:
                # Some Claude relays only support Claude Code protocol and fail
                # on OpenAI-compatible chat/completions. Fallback to local CLI.
                if role == "review" and self._review_cli_fallback_enabled():
                    return self._call_cli(role, prompt)
                raise
        # hybrid
        try:
            result = self._call_cli(role, prompt)
            self._cli_failures = 0
            return result
        except RouterError:
            self._cli_failures += 1
            return self._call_api(role, prompt)

    def _execution_cli_only(self, role: str) -> bool:
        if role not in {"backend", "frontend"}:
            return False
        raw = (os.getenv("EXECUTION_CLI_ONLY", "1") or "").strip().lower()
        return raw in {"1", "true", "yes", "on"}

    def _execution_api_fallback_enabled(self, role: str) -> bool:
        if role not in {"backend", "frontend"}:
            return False
        raw = (os.getenv("EXECUTION_API_FALLBACK", "1") or "").strip().lower()
        return raw in {"1", "true", "yes", "on"}

    def _resolve_model(self, role: str) -> str:
        mapping = {
            "backend": os.getenv("MODEL_BACKEND", "gpt-5.2-codex"),
            "frontend": os.getenv("MODEL_FRONTEND", "gemini-3-flash-preview"),
            "review": os.getenv("MODEL_REVIEW", "claude-opus-4-6"),
            "planning": os.getenv("MODEL_PLANNING", "gpt-5.2-codex"),
            "architect": os.getenv("MODEL_ARCHITECT", "gpt-5.2-codex"),
        }
        return mapping.get(role, os.getenv("MODEL_PLANNING", "gpt-5.2-codex"))

    def _review_cli_fallback_enabled(self) -> bool:
        raw = (os.getenv("REVIEW_CLI_FALLBACK", "1") or "").strip().lower()
        return raw in {"1", "true", "yes", "on"}

    def _provider_credentials(self, provider: str) -> tuple[str | None, str | None]:
        if provider == "openai":
            return (os.getenv("OPENAI_API_BASE"), os.getenv("OPENAI_API_KEY"))
        if provider == "anthropic":
            return (os.getenv("ANTHROPIC_API_BASE"), os.getenv("ANTHROPIC_API_KEY"))
        if provider == "gemini":
            return (os.getenv("GEMINI_API_BASE"), os.getenv("GEMINI_API_KEY"))
        return (None, None)

    def _preferred_providers(self, role: str) -> list[str]:
        # Route by role so each model can use its own relay URL and API key.
        if role == "frontend":
            return ["gemini", "openai", "anthropic"]
        if role == "review":
            return ["anthropic", "openai", "gemini"]
        # backend / planning / architect
        return ["openai", "anthropic", "gemini"]

    def _resolve_claude_cli_model(self, role: str) -> str:
        role_key = role.strip().upper()
        return (
            os.getenv(f"MODEL_CLAUDE_{role_key}")
            or os.getenv("MODEL_CLAUDE_EXECUTION")
            or "claude-opus-4-6"
        )

    def _resolve_gemini_cli_model(self, role: str) -> str:
        role_key = role.strip().upper()
        return (
            os.getenv(f"MODEL_GEMINI_{role_key}")
            or os.getenv("MODEL_GEMINI_EXECUTION")
            or "gemini-2.5-flash"
        )

    def _claude_mcp_config_path(self) -> str | None:
        if not self.working_dir:
            return None
        path = Path(self.working_dir) / "ai-dev-workflow" / "config" / "claude-no-mcp.json"
        if path.exists():
            return str(path)
        return None

    def _cli_timeout_seconds(self, role: str, binary: str) -> int:
        name = Path(binary).stem.lower()
        raw = ""
        if role == "review":
            raw = os.getenv("REVIEW_CLI_TIMEOUT_SECONDS", "600").strip()
        elif name == "claude":
            raw = (
                os.getenv(f"CLAUDE_CLI_TIMEOUT_SECONDS_{role.strip().upper()}")
                or os.getenv("CLAUDE_CLI_TIMEOUT_SECONDS")
                or ""
            ).strip()
        elif name == "codex":
            raw = (
                os.getenv(f"CODEX_CLI_TIMEOUT_SECONDS_{role.strip().upper()}")
                or os.getenv("CODEX_CLI_TIMEOUT_SECONDS")
                or ""
            ).strip()
        elif name == "gemini":
            raw = (
                os.getenv(f"GEMINI_CLI_TIMEOUT_SECONDS_{role.strip().upper()}")
                or os.getenv("GEMINI_CLI_TIMEOUT_SECONDS")
                or ""
            ).strip()
        if not raw:
            return self.timeout_seconds
        try:
            return max(30, int(raw))
        except ValueError:
            return self.timeout_seconds

    def _normalize_api_base(self, base_url: str) -> str:
        normalized = (base_url or "").strip().rstrip("/")
        if not normalized:
            return normalized
        if normalized.endswith("/v1"):
            return normalized
        return f"{normalized}/v1"

    def _select_api_target(self, role: str) -> tuple[str, str, str]:
        checked: list[str] = []
        for provider in self._preferred_providers(role):
            base_url, api_key = self._provider_credentials(provider)
            if base_url and api_key:
                return (provider, self._normalize_api_base(base_url), api_key)
            checked.append(provider)
        raise RouterError(
            "API mode requires at least one configured provider credentials. "
            f"checked={','.join(checked)}"
        )

    def _call_cli(self, role: str, prompt: str) -> RouterResult:
        errors: list[str] = []
        for candidate in self._cli_candidates(role):
            try:
                binary = self._pick_cli_binary([candidate])
            except RouterError as exc:
                errors.append(str(exc))
                continue

            cmd = self._build_cli_command(binary, role, prompt)
            timeout_seconds = self._cli_timeout_seconds(role, binary)
            try:
                proc = subprocess.run(
                    cmd,
                    capture_output=True,
                    text=True,
                    encoding="utf-8",
                    errors="replace",
                    timeout=timeout_seconds,
                    check=False,
                    cwd=self.working_dir,
                )
            except subprocess.TimeoutExpired:
                errors.append(f"CLI timed out for role={role}: {candidate}")
                continue

            if proc.returncode != 0:
                detail = ((proc.stderr or "").strip() or (proc.stdout or "").strip() or str(proc.returncode))
                errors.append(f"CLI failed for role={role} via {candidate}: {detail}")
                continue

            output = (proc.stdout or "").strip()
            if not output:
                errors.append(f"CLI returned empty output for role={role} via {candidate}")
                continue
            return RouterResult(text=output, provider="cli", model=f"cli:{Path(binary).stem.lower()}")

        raise RouterError(" | ".join(errors) if errors else f"No CLI candidate available for role={role}")

    def _cli_candidates(self, role: str) -> list[str]:
        if role == "frontend":
            return ["gemini", "claude", "codex"]
        if role == "review":
            return ["claude", "gemini", "codex"]
        return ["codex", "claude"]

    def _pick_cli_binary(self, candidates: list[str]) -> str:
        for binary in candidates:
            found = shutil.which(binary)
            if found and self._is_cli_healthy(found):
                return found
            resolved = self._resolve_windows_cli(binary)
            if resolved and self._is_cli_healthy(resolved):
                return resolved
        raise RouterError(f"No CLI binary available from: {', '.join(candidates)}")

    def _is_cli_healthy(self, binary: str) -> bool:
        cached = self._cli_health_cache.get(binary)
        if cached is not None:
            return cached
        healthy = False
        name = Path(binary).stem.lower()
        probe_cmd = [binary, "--help"]
        probe_timeout = 20
        if name == "codex":
            probe_cmd = [binary, "-p", "Reply with OK and nothing else."]
            probe_timeout = 30
        try:
            probe = subprocess.run(
                probe_cmd,
                capture_output=True,
                text=True,
                encoding="utf-8",
                errors="replace",
                timeout=probe_timeout,
                check=False,
                cwd=self.working_dir,
            )
            healthy = probe.returncode == 0
        except Exception:
            healthy = False
        self._cli_health_cache[binary] = healthy
        return healthy

    def _resolve_windows_cli(self, binary: str) -> str | None:
        if os.name != "nt":
            return None
        paths: list[Path] = []
        appdata = os.getenv("APPDATA")
        if appdata:
            paths.extend(
                [
                    Path(appdata) / "npm" / f"{binary}.cmd",
                    Path(appdata) / "npm" / f"{binary}.exe",
                ]
            )
        user_home = os.getenv("USERPROFILE")
        if user_home:
            paths.extend(
                [
                    Path(user_home) / ".local" / "bin" / f"{binary}.cmd",
                    Path(user_home) / ".local" / "bin" / f"{binary}.exe",
                    Path(user_home) / ".local" / "bin" / binary,
                ]
            )
        custom_bin = os.getenv(binary.upper())
        if custom_bin:
            custom_path = Path(custom_bin)
            if custom_path.is_dir():
                paths.extend(
                    [
                        custom_path / f"{binary}.cmd",
                        custom_path / f"{binary}.exe",
                        custom_path / binary,
                    ]
                )
            else:
                paths.append(custom_path)
        for path in paths:
            if path.exists():
                return str(path)
        return None

    def _cli_command(self, role: str, prompt: str) -> list[str]:
        binary = self._pick_cli_binary(self._cli_candidates(role))
        return self._build_cli_command(binary, role, prompt)

    def _build_cli_command(self, binary: str, role: str, prompt: str) -> list[str]:
        name = Path(binary).stem.lower()
        if name == "gemini":
            return [
                binary,
                "-p",
                prompt,
                "--model",
                self._resolve_gemini_cli_model(role),
                "--approval-mode",
                "yolo",
                "--sandbox",
                "false",
                "--output-format",
                "text",
            ]
        if name == "claude":
            command = [
                binary,
                "-p",
                "--permission-mode",
                "bypassPermissions",
                "--model",
                self._resolve_claude_cli_model(role),
            ]
            mcp_config = self._claude_mcp_config_path()
            if mcp_config:
                command.extend(
                    [
                        "--strict-mcp-config",
                        "--mcp-config",
                        mcp_config,
                    ]
                )
            command.append(prompt)
            return command
        return [binary, "-p", prompt]

    def _call_api(self, role: str, prompt: str) -> RouterResult:
        model = self._resolve_model(role)
        provider, base_url, api_key = self._select_api_target(role)
        url = base_url.rstrip("/") + "/chat/completions"
        payload = {
            "model": model,
            "messages": [{"role": "user", "content": prompt}],
            "temperature": 0.2,
        }
        max_retries_raw = os.getenv("API_MAX_RETRIES", "3").strip()
        try:
            max_retries = max(1, int(max_retries_raw))
        except ValueError:
            max_retries = 3
        last_error: Exception | None = None
        last_detail = ""
        for attempt in range(1, max_retries + 1):
            request = urllib.request.Request(
                url,
                data=json.dumps(payload).encode("utf-8"),
                headers={
                    "Content-Type": "application/json",
                    "Authorization": f"Bearer {api_key}",
                },
                method="POST",
            )
            try:
                with urllib.request.urlopen(request, timeout=self.timeout_seconds) as resp:
                    body = resp.read().decode("utf-8", errors="ignore")
                break
            except urllib.error.HTTPError as exc:
                detail = exc.read().decode("utf-8", errors="ignore")
                last_error = exc
                last_detail = f"API request failed ({exc.code}): {detail}"
                retriable = exc.code in {408, 409, 425, 429, 500, 502, 503, 504}
                if retriable and attempt < max_retries:
                    time.sleep(min(2 * attempt, 5))
                    continue
                raise RouterError(last_detail) from exc
            except (urllib.error.URLError, TimeoutError) as exc:
                last_error = exc
                reason = getattr(exc, "reason", str(exc))
                last_detail = f"API connection failed: {reason}"
                if attempt < max_retries:
                    time.sleep(min(2 * attempt, 5))
                    continue
                raise RouterError(last_detail) from exc
        else:
            if last_error is not None:
                raise RouterError(last_detail) from last_error
            raise RouterError("API request failed: unknown error")

        data: dict[str, Any] = json.loads(body)
        text = self._extract_content(data)
        usage = data.get("usage") or {}
        self.cost_tracker.add_usage(
            model=model,
            prompt_tokens=usage.get("prompt_tokens"),
            completion_tokens=usage.get("completion_tokens"),
        )
        return RouterResult(text=text, provider=f"api:{provider}", model=model)

    def _extract_content(self, data: dict[str, Any]) -> str:
        choices = data.get("choices") or []
        if not choices:
            raise RouterError(f"API response missing choices: {data}")
        message = choices[0].get("message") or {}
        content = message.get("content")
        if isinstance(content, str):
            if content.strip():
                return content
        if isinstance(content, list):
            chunks: list[str] = []
            for item in content:
                if isinstance(item, dict) and isinstance(item.get("text"), str):
                    chunks.append(item["text"])
            if chunks:
                return "\n".join(chunks)
        raise RouterError(f"Unable to parse API response content: {choices[0]}")
