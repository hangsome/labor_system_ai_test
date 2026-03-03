param(
  [string]$DbHost = '127.0.0.1',
  [int]$DbPort = 3306,
  [string]$User = 'ubuntu',
  [Parameter(Mandatory = $true)]
  [string]$Password,
  [string]$SqlFile = "$PSScriptRoot/incremental_sync.sql"
)

if (-not (Test-Path $SqlFile)) {
  throw "SQL file not found: $SqlFile"
}

$env:MYSQL_PWD = $Password
try {
  Get-Content -Raw -Path $SqlFile | & mysql --default-character-set=utf8mb4 -h $DbHost -P $DbPort -u $User
  if ($LASTEXITCODE -ne 0) {
    throw "mysql execution failed with exit code $LASTEXITCODE"
  }
  Write-Host "Phase12 incremental sync finished."
}
finally {
  Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}
