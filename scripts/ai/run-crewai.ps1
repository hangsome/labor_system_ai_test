param(
  [ValidateSet("development", "maintenance", "legacy-docs")]
  [string]$Profile = "development",
  [string]$RepoRoot = "",
  [string]$Process = "process.md",
  [string]$Todo = "todolist.csv",
  [string]$Output = "docs/crewai/next-actions.md",
  [int]$MaxTasks = 0,
  [string]$Mode = "",
  [int]$TargetPhase = 0,
  [switch]$DryRun,
  [switch]$LegacyTextCrew
)

$ErrorActionPreference = "Stop"
chcp 65001 > $null
$env:PYTHONUTF8 = "1"
$env:PYTHONIOENCODING = "utf-8"

function Set-EnvDefault {
  param(
    [string]$Name,
    [string]$Value
  )
  if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($Name))) {
    [Environment]::SetEnvironmentVariable($Name, $Value, "Process")
    Set-Item -Path ("Env:{0}" -f $Name) -Value $Value
  }
}

if ($LegacyTextCrew) {
  $Profile = "legacy-docs"
}

if ([string]::IsNullOrWhiteSpace($RepoRoot)) {
  $RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
}

$projectPath = Join-Path $RepoRoot "ai-dev-workflow/crewai"
$workflowScript = Join-Path $RepoRoot "ai-dev-workflow/scripts/start.py"
$envPathLocal = Join-Path $projectPath ".env"
$envPathWorkflow = Join-Path $RepoRoot "ai-dev-workflow/config/.env"
$envExampleLocal = Join-Path $projectPath ".env.example"
$envExampleWorkflow = Join-Path $RepoRoot "ai-dev-workflow/config/.env.example"

if (-not (Test-Path $projectPath)) {
  throw "CrewAI project directory not found: $projectPath"
}

if (-not (Get-Command uv -ErrorAction SilentlyContinue)) {
  throw "uv is not installed or not in PATH."
}

$hasLocalEnvFile = Test-Path $envPathLocal
$hasWorkflowEnvFile = Test-Path $envPathWorkflow
if ((-not $hasLocalEnvFile) -and (-not $hasWorkflowEnvFile)) {
  Write-Host "[INFO] Missing env config file."
  Write-Host "[INFO] Local template: $envExampleLocal"
  if (Test-Path $envExampleWorkflow) {
    Write-Host "[INFO] Workflow template: $envExampleWorkflow"
  }
}

$hasOpenAI = -not [string]::IsNullOrWhiteSpace($env:OPENAI_API_KEY)
$hasAnthropic = -not [string]::IsNullOrWhiteSpace($env:ANTHROPIC_API_KEY)
$hasGemini = -not [string]::IsNullOrWhiteSpace($env:GEMINI_API_KEY)
if ((-not $hasOpenAI) -and (-not $hasAnthropic) -and (-not $hasGemini) -and (-not $hasLocalEnvFile) -and (-not $hasWorkflowEnvFile)) {
  throw "Missing model credentials. Set OPENAI_API_KEY/ANTHROPIC_API_KEY/GEMINI_API_KEY in environment, or create ai-dev-workflow/crewai/.env or ai-dev-workflow/config/.env."
}

Push-Location $RepoRoot
try {
  uv sync --project $projectPath
  if ($LASTEXITCODE -ne 0) {
    throw "uv sync failed with exit code $LASTEXITCODE"
  }

  if ($Profile -eq "legacy-docs") {
    if ($MaxTasks -le 0) { $MaxTasks = 8 }
    uv run --project $projectPath labor-workflow-crew --repo-root $RepoRoot --process $Process --todo $Todo --output $Output --max-tasks $MaxTasks --verbose
    $crewExitCode = $LASTEXITCODE

    $resolvedOutput = $Output
    if (-not [System.IO.Path]::IsPathRooted($resolvedOutput)) {
      $resolvedOutput = Join-Path $RepoRoot $Output
    }
    $hasOutput = Test-Path $resolvedOutput
    if ($crewExitCode -ne 0 -and -not $hasOutput) {
      throw "CrewAI legacy text runner failed with exit code $crewExitCode"
    }
    if ($crewExitCode -ne 0 -and $hasOutput) {
      Write-Host "[WARN] Legacy text CrewAI returned exit code $crewExitCode, but output exists: $resolvedOutput"
    }
    if (-not $hasOutput) {
      throw "Legacy text CrewAI finished but output file not found: $resolvedOutput"
    }
    Write-Host "[INFO] Legacy text CrewAI mode completed: $resolvedOutput"
    return
  }

  if (-not (Test-Path $workflowScript)) {
    throw "Workflow start script not found: $workflowScript"
  }

  if ($TargetPhase -le 0) {
    throw "Execution flow requires -TargetPhase when Profile is development or maintenance."
  }

  if (($Process -ne "process.md") -or ($Todo -ne "todolist.csv")) {
    Write-Host "[WARN] -Process/-Todo only apply to legacy-docs profile and are ignored here."
  }

  switch ($Profile) {
    "development" {
      if ($MaxTasks -le 0) { $MaxTasks = 2 }
      if ([string]::IsNullOrWhiteSpace($Mode)) { $Mode = "hybrid" }
      Set-EnvDefault -Name "EXECUTION_CLI_ONLY" -Value "1"
      Set-EnvDefault -Name "EXECUTION_API_FALLBACK" -Value "0"
      Set-EnvDefault -Name "EXECUTION_REVIEW_INTERVAL" -Value "0"
    }
    "maintenance" {
      if ($MaxTasks -le 0) { $MaxTasks = 3 }
      if ([string]::IsNullOrWhiteSpace($Mode)) { $Mode = "hybrid" }
      Set-EnvDefault -Name "EXECUTION_CLI_ONLY" -Value "1"
      Set-EnvDefault -Name "EXECUTION_API_FALLBACK" -Value "1"
      Set-EnvDefault -Name "EXECUTION_REVIEW_INTERVAL" -Value "2"
    }
  }

  $startArgs = @(
    "run",
    "--project", $projectPath,
    "python", $workflowScript,
    "--repo-root", $RepoRoot,
    "--mode", $Mode,
    "--max-tasks", "$MaxTasks"
  )
  if ($TargetPhase -gt 0) {
    $startArgs += @("--target-phase", "$TargetPhase")
  }
  if ($DryRun) {
    $startArgs += "--dry-run"
  }

  $summaryLines = & uv @startArgs
  $flowExitCode = $LASTEXITCODE
  if ($flowExitCode -ne 0) {
    throw "Workflow execution flow failed with exit code $flowExitCode"
  }

  $resolvedOutput = $Output
  if (-not [System.IO.Path]::IsPathRooted($resolvedOutput)) {
    $resolvedOutput = Join-Path $RepoRoot $Output
  }
  $resolvedOutputDir = Split-Path -Parent $resolvedOutput
  if (-not [string]::IsNullOrWhiteSpace($resolvedOutputDir)) {
    New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null
  }

  $summaryText = ($summaryLines -join [Environment]::NewLine).Trim()
  $targetPhaseLabel = "all"
  if ($TargetPhase -gt 0) {
    $targetPhaseLabel = "$TargetPhase"
  }
  $summaryMd = @(
    '# Workflow Flow Summary',
    '',
    "- runner: scripts/ai/run-crewai.ps1",
    "- mode: execution-flow",
    "- profile: $Profile",
    "- repo_root: $RepoRoot",
    "- model_mode: $Mode",
    "- max_tasks: $MaxTasks",
    "- target_phase: $targetPhaseLabel",
    "- dry_run: $([bool]$DryRun)",
    "- execution_cli_only: $($env:EXECUTION_CLI_ONLY)",
    "- execution_api_fallback: $($env:EXECUTION_API_FALLBACK)",
    "- execution_review_interval: $($env:EXECUTION_REVIEW_INTERVAL)",
    '',
    '```json',
    $summaryText,
    '```'
  )
  Set-Content -Path $resolvedOutput -Value $summaryMd -Encoding UTF8

  Write-Host "[INFO] Real execution flow completed. Summary written to: $resolvedOutput"
  Write-Host "[INFO] Use -LegacyTextCrew only when you explicitly want docs-only diagnostic output."
}
finally {
  Pop-Location
}
