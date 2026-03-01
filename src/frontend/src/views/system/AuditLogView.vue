<template>
  <div class="audit-log-view" data-testid="audit-log-view">
    <el-card shadow="never" class="audit-card">
      <template #header>
        <div class="header">
          <h1>Audit Log Center</h1>
          <p>Phase 01 shell for operation audit query and traceability.</p>
        </div>
      </template>

      <el-form :inline="true" class="filter-form" data-testid="audit-filter-form">
        <el-form-item label="Operator">
          <el-input placeholder="Search by operator" />
        </el-form-item>
        <el-form-item label="Action">
          <el-input placeholder="Search by action type" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary">Query</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="[]" border stripe data-testid="audit-log-table">
        <el-table-column prop="createdAt" label="Time" min-width="180" />
        <el-table-column prop="operatorName" label="Operator" min-width="120" />
        <el-table-column prop="actionType" label="Action" min-width="140" />
        <el-table-column prop="targetType" label="Target" min-width="140" />
        <el-table-column prop="targetId" label="Target ID" min-width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.audit-log-view {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(140deg, #f8fafc, #eff6ff 48%, #f1f5f9);
}

.audit-card {
  border-radius: 12px;
}

.header h1 {
  margin: 0;
  font-size: 24px;
  color: #0f172a;
}

.header p {
  margin: 8px 0 0;
  color: #64748b;
}

.filter-form {
  margin-bottom: 12px;
}
</style>
