param(
  [string]$Container = 'labor-system-mysql-1',
  [string]$Db = 'continew_admin',
  [string]$User = 'ubuntu',
  [string]$Password = '',
  [string]$SqlPath = 'backend/continew-server/src/main/resources/db/changelog/mysql/plugin/labor_table.sql'
)

$ErrorActionPreference = 'Stop'
if (-not (Test-Path $SqlPath)) {
  throw "SQL file not found: $SqlPath"
}
if (-not (docker ps --format '{{.Names}}' | Select-String -SimpleMatch $Container)) {
  throw "MySQL container '$Container' is not running"
}
if ([string]::IsNullOrWhiteSpace($Password)) {
  throw 'Password is required. Example: -Password "xu@736107#MH"'
}

$env:MYSQL_PWD = $Password
try {
  Get-Content $SqlPath -Raw | docker exec -i $Container mysql -u$User $Db
  Get-Content $SqlPath -Raw | docker exec -i $Container mysql -u$User $Db

  docker exec $Container mysql -u$User -D $Db -e "
SHOW TABLES LIKE 'labor_lead';
SHOW TABLES LIKE 'labor_employer';
SHOW TABLES LIKE 'labor_contract';
SHOW TABLES LIKE 'labor_settlement_rule';
SHOW TABLES LIKE 'labor_lead_follow_up';
"
}
finally {
  Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}
