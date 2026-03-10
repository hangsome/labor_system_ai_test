# labor-workflow-crew

CrewAI helper project for this repository.

This package is now the legacy text-only diagnostic runner.
For real Stage 4 execution, use the repo-level launcher, which defaults to `EnterpriseDevFlow`.

Run from repo root:

```bash
powershell -ExecutionPolicy Bypass -File scripts/ai/run-crewai.ps1 -Profile development -TargetPhase 3
```

Run the legacy text-only mode explicitly:

```bash
powershell -ExecutionPolicy Bypass -File scripts/ai/run-crewai.ps1 -Profile legacy-docs
```
