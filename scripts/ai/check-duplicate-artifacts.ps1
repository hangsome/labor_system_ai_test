param(
  [string]$RepoRoot = "",
  [string]$ApiRoot = "frontend/src/apis",
  [switch]$FailOnDuplicate
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($RepoRoot)) {
  $RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
}

$targetRoot = Join-Path $RepoRoot $ApiRoot
if (-not (Test-Path $targetRoot)) {
  Write-Host "[INFO] API root not found: $targetRoot"
  exit 0
}

function Normalize-Key {
  param([string]$RelativePath)

  $directory = Split-Path -Parent $RelativePath
  $stem = [System.IO.Path]::GetFileNameWithoutExtension($RelativePath)
  $normalizedStem = ($stem.ToLowerInvariant() -replace "[-_\s]", "")
  $normalizedDirectory = ($directory -replace "\\", "/").ToLowerInvariant()
  return "$normalizedDirectory|$normalizedStem"
}

function Get-RelativePathSafe {
  param(
    [string]$BasePath,
    [string]$FullPath
  )

  $normalizedBase = (Resolve-Path $BasePath).Path.TrimEnd('\')
  $normalizedFull = (Resolve-Path $FullPath).Path
  if ($normalizedFull.StartsWith($normalizedBase, [System.StringComparison]::OrdinalIgnoreCase)) {
    return $normalizedFull.Substring($normalizedBase.Length).TrimStart('\')
  }
  return $normalizedFull
}

$files = Get-ChildItem -Path $targetRoot -Recurse -File -Include *.ts,*.tsx,*.js,*.jsx
$groups = $files |
  ForEach-Object {
    $relativePath = Get-RelativePathSafe -BasePath $targetRoot -FullPath $_.FullName
    [PSCustomObject]@{
      key = Normalize-Key -RelativePath $relativePath
      relative_path = ($relativePath -replace "\\", "/")
    }
  } |
  Group-Object -Property key |
  Where-Object { $_.Count -gt 1 }

if (-not $groups) {
  Write-Host "[OK] No duplicate API artifact groups found under $ApiRoot"
  exit 0
}

Write-Host "[WARN] Duplicate API artifact groups found:"
foreach ($group in $groups) {
  Write-Host ""
  Write-Host ("- key: " + $group.Name)
  foreach ($item in $group.Group) {
    Write-Host ("  * " + $item.relative_path)
  }
}

if ($FailOnDuplicate) {
  exit 2
}

exit 0
