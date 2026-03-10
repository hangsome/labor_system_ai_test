from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


def _repo_root_from_script() -> Path:
    return Path(__file__).resolve().parents[2]


def _workflow_root_from_script() -> Path:
    return Path(__file__).resolve().parents[1]


def main() -> int:
    parser = argparse.ArgumentParser(description="Start workflow flow and persist state.")
    parser.add_argument("--repo-root", default=str(_repo_root_from_script()), help="Repository root path")
    parser.add_argument("--mode", default=None, help="cli | api | hybrid")
    parser.add_argument("--flow-id", default=None, help="Optional explicit flow id to continue")
    parser.add_argument("--max-tasks", type=int, default=8, help="Max tasks included per stage call")
    parser.add_argument("--dry-run", action="store_true", help="Skip real model calls")
    parser.add_argument("--target-phase", type=int, default=None, help="Stop when this phase is completed")
    args = parser.parse_args()

    workflow_root = _workflow_root_from_script()
    if str(workflow_root) not in sys.path:
        sys.path.insert(0, str(workflow_root))

    from orchestrator.flow import EnterpriseDevFlow

    flow = EnterpriseDevFlow(
        repo_root=args.repo_root,
        mode=args.mode,
        flow_id=args.flow_id,
        max_tasks=args.max_tasks,
        dry_run=args.dry_run,
        target_phase=args.target_phase,
    )
    summary = flow.kickoff()
    print(json.dumps(summary, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
