<template>
  <div class="contract-list-page" data-testid="contract-list-view">
    <aside class="filter-side" data-testid="contract-filter-form">
      <h3>合同筛选</h3>

      <section>
        <h4>客户类型</h4>
        <el-checkbox-group v-model="filters.customerTypes" class="checkbox-column">
          <el-checkbox label="酒店" value="酒店" />
          <el-checkbox label="餐饮" value="餐饮" />
          <el-checkbox label="其他" value="其他" />
        </el-checkbox-group>
      </section>

      <section>
        <h4>合同状态</h4>
        <el-checkbox-group v-model="filters.contractStates" class="checkbox-column">
          <el-checkbox label="进行中" value="ACTIVE" />
          <el-checkbox label="已归档" value="ARCHIVED" />
          <el-checkbox label="即将到期" value="EXPIRING" />
          <el-checkbox label="草稿" value="DRAFT" />
        </el-checkbox-group>
      </section>

      <section>
        <h4>客户等级</h4>
        <el-radio-group v-model="filters.level">
          <el-radio value="A">A级（垫资资金类）</el-radio>
          <el-radio value="B">B级（常规合作类）</el-radio>
          <el-radio value="C">C级（新合作类）</el-radio>
        </el-radio-group>
      </section>

      <section>
        <h4>合同周期</h4>
        <el-date-picker v-model="filters.dateRange" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" />
      </section>

      <div class="side-actions">
        <el-button type="primary" @click="queryContracts">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </aside>

    <div class="main-panel">
      <section class="page-head">
        <div>
          <h1>合同管理</h1>
          <p>{{ filteredRows.length }} 个合同</p>
        </div>
        <div class="head-actions">
          <el-button @click="importContracts">导入</el-button>
          <el-button @click="exportContracts">导出</el-button>
          <el-button type="primary" @click="openCreateDialog">新建合同</el-button>
        </div>
      </section>

      <el-card shadow="never" class="search-card">
        <div class="search-wrap">
          <el-input v-model.trim="filters.keyword" placeholder="搜索合同名称、客户名称、合同编号" clearable />
          <el-button @click="queryContracts">高级搜索</el-button>
        </div>
      </el-card>

      <el-card shadow="never" class="table-card">
        <el-table :data="pagedRows" stripe data-testid="contract-table">
          <el-table-column type="selection" width="46" />
          <el-table-column label="合同名称" min-width="220">
            <template #default="{ row }">
              <div class="name-cell">
                <strong>{{ row.contractName }}</strong>
                <span>合同编号：{{ row.contractNo }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="customerName" label="客户名称" min-width="190" />
          <el-table-column prop="customerType" label="客户类型" min-width="120" />
          <el-table-column prop="period" label="合同周期" min-width="180" />
          <el-table-column prop="level" label="客户等级" min-width="100">
            <template #default="{ row }">
              <el-tag type="primary" effect="light">{{ row.level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="合同状态" min-width="120">
            <template #default="{ row }">
              <el-tag :type="getContractStatusTagType(row.status)" effect="light">
                {{ getContractStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="230">
            <template #default="{ row }">
              <router-link
                class="detail-link"
                :to="`/contracts/labor-contracts/${row.contractNo}`"
                data-testid="contract-detail-entry"
              >
                查看
              </router-link>
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="deleteContract(row.contractNo)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <div class="pager-left">
            <span>显示</span>
            <el-select v-model="pageSize" style="width: 90px">
              <el-option :value="5" label="5" />
              <el-option :value="10" label="10" />
            </el-select>
            <span>条，共 {{ filteredRows.length }} 条</span>
          </div>
          <el-pagination
            v-model:current-page="pageNo"
            layout="prev, pager, next"
            :total="filteredRows.length"
            :page-size="pageSize"
          />
        </div>
      </el-card>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新建合同' : '编辑合同'"
      width="620px"
    >
      <el-form label-position="top">
        <el-form-item label="合同编号">
          <el-input v-model.trim="contractForm.contractNo" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="合同名称">
          <el-input v-model.trim="contractForm.contractName" />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model.trim="contractForm.customerName" />
        </el-form-item>
        <el-form-item label="客户类型">
          <el-input v-model.trim="contractForm.customerType" />
        </el-form-item>
        <el-form-item label="合同周期">
          <el-input v-model.trim="contractForm.period" />
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="contractForm.level" style="width: 100%">
            <el-option label="A级" value="A级" />
            <el-option label="B级" value="B级" />
            <el-option label="C级" value="C级" />
          </el-select>
        </el-form-item>
        <el-form-item label="合同状态">
          <el-select v-model="contractForm.status" style="width: 100%">
            <el-option
              v-for="item in contractStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveContract">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  contractStatusTagMap,
  contractStatusTextMap,
  type ContractStatusCode,
} from '../../constants/status'

type ContractRow = {
  contractNo: string
  contractName: string
  customerName: string
  customerType: string
  period: string
  level: string
  status: ContractStatusCode
}

const contractStatusOptions = (Object.keys(contractStatusTextMap) as ContractStatusCode[]).map((key) => ({
  value: key,
  label: contractStatusTextMap[key],
}))

const rows = ref<ContractRow[]>([
  {
    contractNo: 'LC-2026-001',
    contractName: 'XX国际酒店一店-HR2024001',
    customerName: 'XX国际酒店有限公司',
    customerType: '酒店',
    period: '2024-01-01 至 2024-12-31',
    level: 'A级',
    status: 'ACTIVE',
  },
  {
    contractNo: 'LC-2026-002',
    contractName: 'XX度假酒店二店-HR2024002',
    customerName: 'XX度假酒店集团',
    customerType: '酒店',
    period: '2024-02-15 至 2025-02-14',
    level: 'A级',
    status: 'EXPIRING',
  },
  {
    contractNo: 'LC-2026-003',
    contractName: 'XX商务酒店三店-HR2024003',
    customerName: 'XX商务酒店管理有限公司',
    customerType: '酒店',
    period: '2023-11-01 至 2024-10-31',
    level: 'A级',
    status: 'ARCHIVED',
  },
])

const filters = reactive({
  keyword: '',
  customerTypes: ['酒店'] as string[],
  contractStates: ['ACTIVE'] as ContractStatusCode[],
  level: 'A',
  dateRange: [] as string[],
})

const pageSize = ref(5)
const pageNo = ref(1)

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const contractForm = reactive<ContractRow>({
  contractNo: '',
  contractName: '',
  customerName: '',
  customerType: '',
  period: '',
  level: 'A级',
  status: 'DRAFT',
})

const filteredRows = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  return rows.value.filter((row) => {
    const matchesKeyword =
      !keyword ||
      row.contractName.toLowerCase().includes(keyword) ||
      row.customerName.toLowerCase().includes(keyword) ||
      row.contractNo.toLowerCase().includes(keyword)
    const matchesType =
      filters.customerTypes.length === 0 || filters.customerTypes.includes(row.customerType)
    const matchesState =
      filters.contractStates.length === 0 || filters.contractStates.includes(row.status)
    const matchesLevel = !filters.level || row.level.startsWith(filters.level)
    return matchesKeyword && matchesType && matchesState && matchesLevel
  })
})

const pagedRows = computed(() => {
  const start = (pageNo.value - 1) * pageSize.value
  return filteredRows.value.slice(start, start + pageSize.value)
})

watch(
  () => [filters.keyword, filters.customerTypes, filters.contractStates, filters.level],
  () => {
    pageNo.value = 1
  }
)

function getContractStatusTagType(status: ContractStatusCode) {
  return contractStatusTagMap[status] ?? 'info'
}

function getContractStatusText(status: ContractStatusCode) {
  return contractStatusTextMap[status] ?? '未知状态'
}

function queryContracts() {
  ElMessage.success('已按筛选条件查询')
}

function resetFilters() {
  filters.keyword = ''
  filters.customerTypes = []
  filters.contractStates = []
  filters.level = ''
  filters.dateRange = []
  ElMessage.info('已重置合同筛选条件')
}

function importContracts() {
  ElMessage.info('合同导入功能待接入文件服务')
}

function exportContracts() {
  ElMessage.success(`已导出 ${filteredRows.value.length} 条合同`) 
}

function openCreateDialog() {
  dialogMode.value = 'create'
  contractForm.contractNo = `LC-2026-${String(rows.value.length + 1).padStart(3, '0')}`
  contractForm.contractName = ''
  contractForm.customerName = ''
  contractForm.customerType = '酒店'
  contractForm.period = ''
  contractForm.level = 'A级'
  contractForm.status = 'DRAFT'
  dialogVisible.value = true
}

function openEditDialog(row: ContractRow) {
  dialogMode.value = 'edit'
  contractForm.contractNo = row.contractNo
  contractForm.contractName = row.contractName
  contractForm.customerName = row.customerName
  contractForm.customerType = row.customerType
  contractForm.period = row.period
  contractForm.level = row.level
  contractForm.status = row.status
  dialogVisible.value = true
}

function saveContract() {
  if (!contractForm.contractNo || !contractForm.contractName || !contractForm.customerName) {
    ElMessage.error('请完整填写合同信息')
    return
  }

  if (dialogMode.value === 'create') {
    const exists = rows.value.some((item) => item.contractNo === contractForm.contractNo)
    if (exists) {
      ElMessage.error('合同编号已存在')
      return
    }
    rows.value.unshift({ ...contractForm })
    ElMessage.success('合同创建成功')
  } else {
    const index = rows.value.findIndex((item) => item.contractNo === contractForm.contractNo)
    if (index >= 0) {
      rows.value[index] = { ...contractForm }
      ElMessage.success('合同更新成功')
    }
  }

  dialogVisible.value = false
}

function deleteContract(contractNo: string) {
  rows.value = rows.value.filter((item) => item.contractNo !== contractNo)
  ElMessage.success('合同已删除')
}
</script>

<style scoped>
.contract-list-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 14px;
  padding: 20px;
  background: #f3f5f9;
}

.filter-side,
.search-card,
.table-card {
  border-radius: 12px;
  border: 1px solid #edf0f6;
  background: #fff;
}

.filter-side {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-side h3 {
  margin: 0;
  font-size: 30px;
}

.filter-side h4 {
  margin: 0 0 10px;
  font-size: 20px;
  color: #374151;
}

.checkbox-column,
:deep(.el-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.side-actions {
  display: flex;
  gap: 10px;
}

.main-panel {
  min-width: 0;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.page-head h1 {
  margin: 0;
  font-size: 42px;
  line-height: 1.1;
}

.page-head p {
  margin: 8px 0 0;
  color: #2563eb;
  font-weight: 600;
}

.head-actions {
  display: flex;
  gap: 8px;
}

.search-card {
  margin-top: 12px;
  padding: 14px;
}

.search-wrap {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.table-card {
  margin-top: 12px;
}

.name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name-cell strong {
  color: #1f2937;
}

.name-cell span {
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
  padding: 0 8px 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.pager-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 13px;
}

@media (max-width: 1240px) {
  .contract-list-page {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .contract-list-page {
    padding: 12px;
  }

  .page-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-wrap {
    grid-template-columns: 1fr;
  }

  .pagination-wrap {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
