<template>
  <div class="lead-list-page" data-testid="lead-list-view">
    <section class="page-head">
      <div>
        <h1>客户线索管理</h1>
        <p>管理和跟踪所有客户线索及合作进展</p>
      </div>
      <div class="head-actions">
        <el-button @click="importLeads">导入线索</el-button>
        <el-button type="primary" data-testid="lead-create-entry" @click="openCreateDialog">
          新建线索
        </el-button>
      </div>
    </section>

    <section class="metrics-grid">
      <el-card shadow="never" class="metric-card">
        <h4>线索总量</h4>
        <p>{{ rows.length }}</p>
        <small>↑ 12.5%</small>
      </el-card>
      <el-card shadow="never" class="metric-card">
        <h4>日均转化率</h4>
        <p>8.3</p>
        <small>↑ 3.2%</small>
      </el-card>
      <el-card shadow="never" class="metric-card">
        <h4>待跟进线索</h4>
        <p>{{ pendingCount }}</p>
        <small class="warn">近3个月</small>
      </el-card>
      <el-card shadow="never" class="metric-card">
        <h4>线索转化率</h4>
        <p>28.7%</p>
        <small class="danger">↓ 1.8%</small>
      </el-card>
    </section>

    <el-card shadow="never" class="trend-card">
      <template #header>
        <div class="card-title-row">
          <h3>线索增长趋势</h3>
          <div class="tab-group">
            <button class="tab-btn active">周</button>
            <button class="tab-btn">月</button>
            <button class="tab-btn">季</button>
          </div>
        </div>
      </template>
      <svg viewBox="0 0 760 220" class="trend-svg" aria-hidden="true">
        <g stroke="#e5e7eb" stroke-width="1">
          <line x1="40" y1="180" x2="720" y2="180" />
          <line x1="40" y1="140" x2="720" y2="140" />
          <line x1="40" y1="100" x2="720" y2="100" />
          <line x1="40" y1="60" x2="720" y2="60" />
        </g>
        <polyline
          points="60,150 140,126 220,138 300,104 380,88 460,80 540,110 620,90 700,80"
          fill="none"
          stroke="#3b82f6"
          stroke-width="3"
          stroke-linecap="round"
        />
        <polygon
          points="60,150 140,126 220,138 300,104 380,88 460,80 540,110 620,90 700,80 700,180 60,180"
          fill="rgba(59,130,246,0.14)"
        />
      </svg>
    </el-card>

    <el-card shadow="never" class="filter-card">
      <template #header>
        <h3>线索筛选</h3>
      </template>

      <el-form class="filter-form" data-testid="lead-filter-form">
        <el-form-item label="项目名称">
          <el-input v-model.trim="filters.keyword" placeholder="请输入项目名称" clearable />
        </el-form-item>
        <el-form-item label="业务开发人">
          <el-select v-model="filters.owner" placeholder="请选择开发人" clearable>
            <el-option label="王磊" value="王磊" />
            <el-option label="李婷" value="李婷" />
            <el-option label="张晨" value="张晨" />
            <el-option label="周敏" value="周敏" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户行业">
          <el-select v-model="filters.industry" placeholder="请选择行业" clearable>
            <el-option label="酒店" value="酒店" />
            <el-option label="连锁门店" value="连锁门店" />
            <el-option label="物流" value="物流" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作状态">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable>
            <el-option
              v-for="item in leadStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <div class="filter-actions">
          <el-button @click="resetFilters" data-testid="lead-reset-button">重置筛选</el-button>
          <el-button type="primary" data-testid="lead-search-button" @click="queryLeads">查询</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-title-row">
          <h3>客户线索列表</h3>
          <div class="table-actions">
            <el-button :disabled="selectedLeadCodes.length === 0" @click="batchDelete">批量删除</el-button>
            <el-button @click="exportLeads">导出</el-button>
          </div>
        </div>
      </template>

      <el-table :data="pagedRows" stripe data-testid="lead-table" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="46" />
        <el-table-column label="项目信息" min-width="280">
          <template #default="{ row }">
            <div class="project-cell">
              <strong>{{ row.projectName }}</strong>
              <span>{{ row.leadCode }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="industry" label="客户行业" min-width="120" />
        <el-table-column prop="bizOwner" label="业务开发人" min-width="120" />
        <el-table-column label="合作状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="getLeadStatusTagType(row.cooperationStatus)" effect="light">
              {{ getLeadStatusText(row.cooperationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="最近时间" min-width="160" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <router-link class="detail-link" :to="`/crm/leads/${row.leadCode}`">查看</router-link>
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteLead(row.leadCode)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <span>显示 {{ pageSize }} 条/页，共 {{ filteredRows.length }} 条</span>
        <el-pagination
          v-model:current-page="pageNo"
          :page-size="pageSize"
          layout="prev, pager, next"
          :total="filteredRows.length"
          data-testid="lead-pagination"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新建线索' : '编辑线索'" width="560px">
      <el-form label-position="top">
        <el-form-item label="线索编号">
          <el-input v-model.trim="leadForm.leadCode" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input v-model.trim="leadForm.projectName" />
        </el-form-item>
        <el-form-item label="客户行业">
          <el-input v-model.trim="leadForm.industry" />
        </el-form-item>
        <el-form-item label="业务开发人">
          <el-input v-model.trim="leadForm.bizOwner" />
        </el-form-item>
        <el-form-item label="合作状态">
          <el-select v-model="leadForm.cooperationStatus" style="width: 100%">
            <el-option
              v-for="item in leadStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveLead">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { leadStatusTagMap, leadStatusTextMap, type LeadStatusCode } from '../../constants/status'

type LeadRow = {
  leadCode: string
  projectName: string
  industry: string
  bizOwner: string
  cooperationStatus: LeadStatusCode
  updatedAt: string
}

const leadStatusOptions = (Object.keys(leadStatusTextMap) as LeadStatusCode[]).map((key) => ({
  value: key,
  label: leadStatusTextMap[key],
}))

const rows = ref<LeadRow[]>([
  {
    leadCode: 'LEAD-001',
    projectName: '上海浦东希尔顿酒店派遣项目',
    industry: '酒店',
    bizOwner: '王磊',
    cooperationStatus: 'FOLLOWING',
    updatedAt: '2026-02-27 09:10',
  },
  {
    leadCode: 'LEAD-002',
    projectName: '北京王府井商场保洁派遣项目',
    industry: '连锁门店',
    bizOwner: '李婷',
    cooperationStatus: 'NEW',
    updatedAt: '2026-02-27 08:35',
  },
  {
    leadCode: 'LEAD-003',
    projectName: '广州白云国际机场餐饮服务项目',
    industry: '酒店',
    bizOwner: '张晨',
    cooperationStatus: 'WON',
    updatedAt: '2026-02-26 21:15',
  },
  {
    leadCode: 'LEAD-004',
    projectName: '深圳科技园安保派遣项目',
    industry: '物流',
    bizOwner: '周敏',
    cooperationStatus: 'FOLLOWING',
    updatedAt: '2026-02-26 18:25',
  },
  {
    leadCode: 'LEAD-005',
    projectName: '杭州西湖景区餐饮配送项目',
    industry: '连锁门店',
    bizOwner: '黄凯',
    cooperationStatus: 'LOST',
    updatedAt: '2026-02-25 16:40',
  },
  {
    leadCode: 'LEAD-006',
    projectName: '滨海酒店季节性增援项目',
    industry: '酒店',
    bizOwner: '陈楠',
    cooperationStatus: 'NEW',
    updatedAt: '2026-02-25 14:05',
  },
])

const filters = reactive({
  keyword: '',
  owner: '',
  industry: '',
  status: '',
})

const pageNo = ref(1)
const pageSize = 5
const selectedLeadCodes = ref<string[]>([])

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const leadForm = reactive<LeadRow>({
  leadCode: '',
  projectName: '',
  industry: '',
  bizOwner: '',
  cooperationStatus: 'NEW',
  updatedAt: '',
})

const pendingCount = computed(() => rows.value.filter((item) => item.cooperationStatus === 'FOLLOWING').length)

const filteredRows = computed(() => {
  return rows.value.filter((row) => {
    const keyword = filters.keyword.trim().toLowerCase()
    const matchesKeyword =
      keyword.length === 0 ||
      row.projectName.toLowerCase().includes(keyword) ||
      row.leadCode.toLowerCase().includes(keyword)
    const matchesStatus = !filters.status || row.cooperationStatus === filters.status
    const matchesIndustry = !filters.industry || row.industry === filters.industry
    const matchesOwner = !filters.owner || row.bizOwner === filters.owner
    return matchesKeyword && matchesStatus && matchesIndustry && matchesOwner
  })
})

const pagedRows = computed(() => {
  const start = (pageNo.value - 1) * pageSize
  return filteredRows.value.slice(start, start + pageSize)
})

watch(
  () => [filters.keyword, filters.status, filters.industry, filters.owner],
  () => {
    pageNo.value = 1
  }
)

function nowTimestamp() {
  const date = new Date()
  const yyyy = date.getFullYear()
  const MM = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const HH = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${yyyy}-${MM}-${dd} ${HH}:${mm}`
}

function getLeadStatusTagType(status: string) {
  return leadStatusTagMap[status as LeadStatusCode] ?? 'info'
}

function getLeadStatusText(status: string) {
  return leadStatusTextMap[status as LeadStatusCode] ?? '未知状态'
}

function queryLeads() {
  ElMessage.success('已按筛选条件查询')
}

function resetFilters() {
  filters.keyword = ''
  filters.owner = ''
  filters.industry = ''
  filters.status = ''
  ElMessage.info('已重置筛选条件')
}

function importLeads() {
  ElMessage.info('导入线索功能待接入文件服务')
}

function exportLeads() {
  ElMessage.success(`已导出 ${filteredRows.value.length} 条线索`) 
}

function openCreateDialog() {
  dialogMode.value = 'create'
  leadForm.leadCode = `LEAD-${String(rows.value.length + 1).padStart(3, '0')}`
  leadForm.projectName = ''
  leadForm.industry = ''
  leadForm.bizOwner = ''
  leadForm.cooperationStatus = 'NEW'
  leadForm.updatedAt = nowTimestamp()
  dialogVisible.value = true
}

function openEditDialog(row: LeadRow) {
  dialogMode.value = 'edit'
  leadForm.leadCode = row.leadCode
  leadForm.projectName = row.projectName
  leadForm.industry = row.industry
  leadForm.bizOwner = row.bizOwner
  leadForm.cooperationStatus = row.cooperationStatus
  leadForm.updatedAt = row.updatedAt
  dialogVisible.value = true
}

function saveLead() {
  if (!leadForm.leadCode || !leadForm.projectName || !leadForm.bizOwner) {
    ElMessage.error('请完整填写线索信息')
    return
  }

  if (dialogMode.value === 'create') {
    const exists = rows.value.some((item) => item.leadCode === leadForm.leadCode)
    if (exists) {
      ElMessage.error('线索编号已存在')
      return
    }
    rows.value.unshift({ ...leadForm, updatedAt: nowTimestamp() })
    ElMessage.success('线索创建成功')
  } else {
    const index = rows.value.findIndex((item) => item.leadCode === leadForm.leadCode)
    if (index >= 0) {
      rows.value[index] = { ...leadForm, updatedAt: nowTimestamp() }
      ElMessage.success('线索更新成功')
    }
  }

  dialogVisible.value = false
}

function deleteLead(leadCode: string) {
  rows.value = rows.value.filter((item) => item.leadCode !== leadCode)
  ElMessage.success('线索已删除')
}

function onSelectionChange(selected: LeadRow[]) {
  selectedLeadCodes.value = selected.map((item) => item.leadCode)
}

function batchDelete() {
  if (selectedLeadCodes.value.length === 0) {
    ElMessage.warning('请先选择线索')
    return
  }
  rows.value = rows.value.filter((item) => !selectedLeadCodes.value.includes(item.leadCode))
  selectedLeadCodes.value = []
  ElMessage.success('批量删除完成')
}
</script>

<style scoped>
.lead-list-page {
  min-height: 100vh;
  padding: 22px;
  background: #f3f5f9;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.page-head h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.15;
  color: #1f2937;
}

.page-head p {
  margin: 8px 0 0;
  color: #6b7280;
}

.head-actions {
  display: flex;
  gap: 10px;
}

.metrics-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric-card {
  border-radius: 12px;
  border: 1px solid #edf0f6;
}

.metric-card h4 {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.metric-card p {
  margin: 10px 0 8px;
  font-size: 42px;
  font-weight: 700;
  line-height: 1;
  color: #111827;
}

.metric-card small {
  color: #22c55e;
  font-size: 13px;
  font-weight: 600;
}

.metric-card small.warn {
  color: #f59e0b;
}

.metric-card small.danger {
  color: #ef4444;
}

.trend-card,
.filter-card,
.table-card {
  margin-top: 14px;
  border-radius: 12px;
  border: 1px solid #edf0f6;
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.card-title-row h3 {
  margin: 0;
  font-size: 18px;
}

.tab-group {
  display: inline-flex;
  gap: 6px;
}

.tab-btn {
  border: 0;
  border-radius: 6px;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 12px;
  padding: 4px 10px;
}

.tab-btn.active {
  color: #1f2937;
  background: #dbeafe;
}

.trend-svg {
  width: 100%;
  height: 230px;
}

.filter-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 12px;
}

:deep(.filter-form .el-form-item) {
  margin-bottom: 0;
}

.filter-actions {
  grid-column: 1 / -1;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.project-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.project-cell strong {
  color: #2563eb;
}

.project-cell span {
  color: #94a3b8;
  font-size: 12px;
}

.detail-link {
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
  margin-right: 8px;
}

.pagination-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 1260px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-form {
    grid-template-columns: repeat(2, minmax(160px, 1fr));
  }
}

@media (max-width: 760px) {
  .lead-list-page {
    padding: 14px;
  }

  .page-head {
    flex-direction: column;
  }

  .metrics-grid,
  .filter-form {
    grid-template-columns: 1fr;
  }

  .pagination-wrap {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}
</style>
