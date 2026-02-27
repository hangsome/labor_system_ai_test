$ErrorActionPreference = 'Stop'

$migrationPath = 'src/backend/src/main/resources/db/migration/V3__phase02_crm_contract_baseline.sql'
$migrationSql = Get-Content $migrationPath -Raw

# Run twice to verify idempotency.
$migrationSql | docker exec -i labor-system-mysql-1 mysql -ulabor -plabor123 labor_system
$migrationSql | docker exec -i labor-system-mysql-1 mysql -ulabor -plabor123 labor_system

docker exec labor-system-mysql-1 mysql -ulabor -plabor123 -D labor_system -e "
SHOW TABLES LIKE 'customer_lead';
SHOW TABLES LIKE 'employer_unit';
SHOW TABLES LIKE 'labor_contract';
SHOW TABLES LIKE 'settlement_rule';
SHOW INDEX FROM settlement_rule WHERE Key_name = 'uk_rule_contract_version';
SELECT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'employer_unit'
  AND CONSTRAINT_NAME = 'fk_employer_unit_lead';
SELECT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'labor_contract'
  AND CONSTRAINT_NAME = 'fk_labor_contract_employer_unit';
SELECT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'settlement_rule'
  AND CONSTRAINT_NAME = 'fk_settlement_rule_contract';
"
