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
    parser = argparse.ArgumentParser(description="Resume an existing flow by flow-id.")
    parser.add_argument("flow_id", help="Flow id to resume")
    parser.add_argument("--repo-root", default=str(_repo_root_from_script()), help="Repository root path")
    parser.add_argument("--mode", default=None, help="Optional mode override")
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
    summary = flow.kickoff(inputs={"id": args.flow_id})
    print(json.dumps(summary, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
