$ErrorActionPreference = 'Stop'

Get-Content 'src/backend/src/main/resources/db/migration/V2__phase01_iam_platform_baseline.sql' -Raw |
  docker exec -i labor-system-mysql-1 mysql -ulabor -plabor123 labor_system

docker exec labor-system-mysql-1 mysql -ulabor -plabor123 -D labor_system -e "
SHOW TABLES LIKE 'user_account';
SHOW TABLES LIKE 'role';
SHOW TABLES LIKE 'permission';
SHOW TABLES LIKE 'data_scope_policy';
SHOW TABLES LIKE 'audit_log';
"
