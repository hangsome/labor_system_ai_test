<template>
  <div class="lead-list-page" data-testid="lead-list-view">
    <el-card class="panel" shadow="hover">
      <template #header>
        <div class="panel-header">
          <div>
            <h2>客户线索列表</h2>
            <p>支持按项目关键词和合作状态筛选，并按分页查看。</p>
          </div>
          <el-button type="primary" plain data-testid="lead-create-entry">新建线索</el-button>
        </div>
      </template>

      <el-form class="filter-form" inline data-testid="lead-filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="filters.keyword"
            placeholder="项目名/线索编号"
            clearable
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="合作状态">
          <el-select
            v-model="filters.status"
            clearable
            placeholder="全部状态"
            style="width: 180px"
          >
            <el-option label="NEW" value="NEW" />
            <el-option label="FOLLOWING" value="FOLLOWING" />
            <el-option label="WON" value="WON" />
            <el-option label="LOST" value="LOST" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" data-testid="lead-search-button">查询</el-button>
          <el-button @click="resetFilters" data-testid="lead-reset-button">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="pagedRows" stripe data-testid="lead-table">
        <el-table-column prop="leadCode" label="线索编号" min-width="130" />
        <el-table-column prop="projectName" label="项目名称" min-width="220" />
        <el-table-column prop="bizOwner" label="业务负责人" min-width="130" />
        <el-table-column prop="cooperationStatus" label="合作状态" min-width="130" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNo"
          :page-size="pageSize"
          layout="prev, pager, next, total"
          :total="filteredRows.length"
          data-testid="lead-pagination"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'

type LeadRow = {
  leadCode: string
  projectName: string
  bizOwner: string
  cooperationStatus: 'NEW' | 'FOLLOWING' | 'WON' | 'LOST'
  updatedAt: string
}

const rows = ref<LeadRow[]>([
  {
    leadCode: 'LEAD-001',
    projectName: '华东酒店劳务外包项目',
    bizOwner: '王磊',
    cooperationStatus: 'FOLLOWING',
    updatedAt: '2026-02-27 09:10',
  },
  {
    leadCode: 'LEAD-002',
    projectName: '华南商场保洁服务项目',
    bizOwner: '李婷',
    cooperationStatus: 'NEW',
    updatedAt: '2026-02-27 08:35',
  },
  {
    leadCode: 'LEAD-003',
    projectName: '华北园区安保服务项目',
    bizOwner: '张晨',
    cooperationStatus: 'WON',
    updatedAt: '2026-02-26 21:15',
  },
  {
    leadCode: 'LEAD-004',
    projectName: '西南物流分拣外包项目',
    bizOwner: '周敏',
    cooperationStatus: 'FOLLOWING',
    updatedAt: '2026-02-26 18:25',
  },
  {
    leadCode: 'LEAD-005',
    projectName: '华中连锁门店导购项目',
    bizOwner: '黄凯',
    cooperationStatus: 'LOST',
    updatedAt: '2026-02-25 16:40',
  },
  {
    leadCode: 'LEAD-006',
    projectName: '滨海酒店季节性增援项目',
    bizOwner: '陈楠',
    cooperationStatus: 'NEW',
    updatedAt: '2026-02-25 14:05',
  },
])

const filters = reactive({
  keyword: '',
  status: '',
})

const pageNo = ref(1)
const pageSize = 3

const filteredRows = computed(() => {
  return rows.value.filter((row) => {
    const keyword = filters.keyword.trim().toLowerCase()
    const matchesKeyword =
      keyword.length === 0 ||
      row.projectName.toLowerCase().includes(keyword) ||
      row.leadCode.toLowerCase().includes(keyword)
    const matchesStatus = !filters.status || row.cooperationStatus === filters.status
    return matchesKeyword && matchesStatus
  })
})

const pagedRows = computed(() => {
  const start = (pageNo.value - 1) * pageSize
  return filteredRows.value.slice(start, start + pageSize)
})

watch(
  () => [filters.keyword, filters.status],
  () => {
    pageNo.value = 1
  }
)

function resetFilters() {
  filters.keyword = ''
  filters.status = ''
}
</script>

<style scoped>
.lead-list-page {
  padding: 24px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.panel-header h2 {
  margin: 0;
  font-size: 22px;
  color: #1f2937;
}

.panel-header p {
  margin: 6px 0 0;
  color: #6b7280;
}

.filter-form {
  margin-bottom: 14px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
