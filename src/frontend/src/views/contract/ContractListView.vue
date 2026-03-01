<template>
  <div class="contract-list-page" data-testid="contract-list-view">
    <el-card shadow="never" class="contract-card">
      <template #header>
        <div class="header">
          <h1>Labor Contract List</h1>
          <p>Phase 2 shell for contract lifecycle list and quick actions.</p>
        </div>
      </template>

      <el-form :inline="true" class="filter-form" data-testid="contract-filter-form">
        <el-form-item label="Keyword">
          <el-input v-model.trim="filters.keyword" placeholder="Contract no or lead code" clearable />
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="filters.status" placeholder="All" clearable style="width: 180px">
            <el-option label="DRAFT" value="DRAFT" />
            <el-option label="ACTIVE" value="ACTIVE" />
            <el-option label="TERMINATED" value="TERMINATED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">Query</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="filteredRows" stripe border data-testid="contract-table">
        <el-table-column prop="contractNo" label="Contract No" min-width="150" />
        <el-table-column prop="leadCode" label="Lead Code" min-width="130" />
        <el-table-column prop="employerUnit" label="Employer Unit" min-width="220" />
        <el-table-column prop="status" label="Status" min-width="120" />
        <el-table-column label="Action" width="140">
          <template #default="{ row }">
            <router-link
              class="detail-link"
              :to="`/contracts/labor-contracts/${row.contractNo}`"
              data-testid="contract-detail-entry"
            >
              View Detail
            </router-link>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'

type ContractRow = {
  contractNo: string
  leadCode: string
  employerUnit: string
  status: 'DRAFT' | 'ACTIVE' | 'TERMINATED'
}

const rows = ref<ContractRow[]>([
  {
    contractNo: 'LC-2026-001',
    leadCode: 'LEAD-001',
    employerUnit: 'Huadong Hotel Group',
    status: 'ACTIVE',
  },
  {
    contractNo: 'LC-2026-002',
    leadCode: 'LEAD-002',
    employerUnit: 'South Mall Services',
    status: 'DRAFT',
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
      row.contractNo.toLowerCase().includes(keyword) ||
      row.leadCode.toLowerCase().includes(keyword)
    const matchesStatus = !filters.status || row.status === filters.status
    return matchesKeyword && matchesStatus
  })
})
</script>

<style scoped>
.contract-list-page {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(140deg, #f8fafc, #fef9c3 25%, #f1f5f9);
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

.detail-link {
  color: #1d4ed8;
  text-decoration: none;
  font-weight: 600;
}
</style>
