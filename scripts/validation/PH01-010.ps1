param(
  [string]$Container = 'labor-system-mysql-1',
  [string]$Db = 'continew_admin',
  [string]$User = 'ubuntu',
  [string]$Password = ''
)

$ErrorActionPreference = 'Stop'
if (-not (docker ps --format '{{.Names}}' | Select-String -SimpleMatch $Container)) {
  throw "MySQL container '$Container' is not running"
}
if ([string]::IsNullOrWhiteSpace($Password)) {
  throw 'Password is required. Example: -Password "xu@736107#MH"'
}

$env:MYSQL_PWD = $Password
try {
  docker exec $Container mysql -u$User -D $Db -e "
SHOW TABLES LIKE 'sys_user';
SHOW TABLES LIKE 'sys_role';
SHOW TABLES LIKE 'sys_menu';
SHOW TABLES LIKE 'sys_user_role';
SHOW TABLES LIKE 'sys_role_menu';
SHOW TABLES LIKE 'sys_log';
"
}
finally {
  Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}
