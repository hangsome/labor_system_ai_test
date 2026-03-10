param(
  [string]$RepoRoot = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($RepoRoot)) {
  $RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
}

$paths = @(
  ".crewai/flows",
  "docs/crewai",
  ".tmp",
  ".tmp-cli-tests"
)

foreach ($relativePath in $paths) {
  $targetPath = Join-Path $RepoRoot $relativePath
  if (-not (Test-Path $targetPath)) {
    continue
  }
  Remove-Item -Path $targetPath -Recurse -Force
  Write-Host "[CLEANED] $relativePath"
}

Write-Host "[DONE] Workflow runtime artifacts removed."
