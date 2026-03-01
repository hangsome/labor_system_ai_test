# Quality Baseline: main@348462b

## Scope
- Worktree: `f:\project\ai project\劳务系统_baseline_main`
- Commit: `348462b`
- Compare target: `exp/multi-agent-ph01-ph02-rerun`

## Validation Commands
| Command | Exit Code | Key Result |
|---|---:|---|
| `mvn -B -f src/backend/pom.xml test` | 0 | `Tests run: 99, Failures: 0, Errors: 0, Skipped: 1` |
| `npm --prefix src/frontend run test` | 1 | `ERR_MODULE_NOT_FOUND: vitest` |
| `npm --prefix src/frontend run test:e2e` | 1 | `playwright: unknown command 'test'` |

## Quality Interpretation
- Backend regression baseline is healthy.
- Frontend unit/e2e checks are blocked by local dependency/CLI mismatch, not by assertion failures.
- This baseline is valid for parity comparison, but not sufficient for full frontend quality conclusion.
