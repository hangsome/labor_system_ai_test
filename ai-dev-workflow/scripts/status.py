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
    parser = argparse.ArgumentParser(description="Show flow state or list available flows.")
    parser.add_argument("--repo-root", default=str(_repo_root_from_script()), help="Repository root path")
    parser.add_argument("--flow-id", default=None, help="Show single flow details")
    parser.add_argument("--list", action="store_true", help="List all flow states")
    args = parser.parse_args()

    workflow_root = _workflow_root_from_script()
    if str(workflow_root) not in sys.path:
        sys.path.insert(0, str(workflow_root))

    from orchestrator.flow import list_flow_states, load_flow_state

    if args.flow_id:
        state = load_flow_state(args.repo_root, args.flow_id)
        print(json.dumps(state.model_dump(), ensure_ascii=False, indent=2))
        return 0

    states = list_flow_states(args.repo_root)
    if args.list or not states:
        payload = [s.model_dump() for s in states]
        print(json.dumps(payload, ensure_ascii=False, indent=2))
        return 0

    print(json.dumps(states[0].model_dump(), ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

