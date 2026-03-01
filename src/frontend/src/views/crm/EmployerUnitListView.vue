<template>
  <div class="employer-unit-page" data-testid="employer-unit-view">
    <el-card shadow="never" class="employer-card">
      <template #header>
        <div class="header">
          <h1>Employer Unit List</h1>
          <p>Phase 2 shell for employer management and status maintenance.</p>
        </div>
      </template>

      <el-form :inline="true" class="filter-form" data-testid="employer-unit-filter-form">
        <el-form-item label="Keyword">
          <el-input v-model.trim="filters.keyword" placeholder="Unit name or code" clearable />
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="filters.status" placeholder="All" clearable style="width: 160px">
            <el-option label="ACTIVE" value="ACTIVE" />
            <el-option label="INACTIVE" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">Query</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="filteredRows" stripe border data-testid="employer-unit-table">
        <el-table-column prop="unitCode" label="Unit Code" min-width="130" />
        <el-table-column prop="unitName" label="Unit Name" min-width="220" />
        <el-table-column prop="contactName" label="Contact" min-width="140" />
        <el-table-column prop="status" label="Status" min-width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'

type EmployerUnitRow = {
  unitCode: string
  unitName: string
  contactName: string
  status: 'ACTIVE' | 'INACTIVE'
}

const rows = ref<EmployerUnitRow[]>([
  {
    unitCode: 'EMP-001',
    unitName: 'Huadong Hotel Group',
    contactName: 'Wang Lei',
    status: 'ACTIVE',
  },
  {
    unitCode: 'EMP-002',
    unitName: 'South Mall Services',
    contactName: 'Li Na',
    status: 'INACTIVE',
  },
])

const filters = reactive({
  keyword: '',
  status: '',
})

const filteredRows = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  return rows.value.filter((row) => {
    const matchesKeyword =
      !keyword ||
      row.unitName.toLowerCase().includes(keyword) ||
      row.unitCode.toLowerCase().includes(keyword)
    const matchesStatus = !filters.status || row.status === filters.status
    return matchesKeyword && matchesStatus
  })
})
</script>

<style scoped>
.employer-unit-page {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(140deg, #f8fafc, #eff6ff 46%, #f1f5f9);
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
