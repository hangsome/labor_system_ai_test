<template>
  <div class="employer-unit-page" data-testid="employer-unit-view">
    <section class="page-head">
      <div>
        <h1>用工单位管理</h1>
        <p>管理所有用工单位信息、合同及合作状态</p>
      </div>
      <div class="head-actions">
        <el-button @click="importUnits">导入</el-button>
        <el-button type="primary" @click="openCreateDialog">添加用工单位</el-button>
      </div>
    </section>

    <el-card shadow="never" class="filter-card">
      <el-form class="filter-form" data-testid="employer-unit-filter-form">
        <el-form-item label="客户名称">
          <el-input v-model.trim="filters.keyword" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="客户类型">
          <el-select v-model="filters.customerType" placeholder="全部类型" clearable>
            <el-option label="五星级酒店" value="五星级酒店" />
            <el-option label="连锁酒店" value="连锁酒店" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="filters.level" placeholder="全部等级" clearable>
            <el-option label="A级" value="A级" />
            <el-option label="B级" value="B级" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable>
            <el-option label="已合作" value="ACTIVE" />
            <el-option label="待签约" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务开发人">
          <el-select v-model="filters.owner" placeholder="全部人员" clearable>
            <el-option label="李军" value="李军" />
            <el-option label="王芳" value="王芳" />
            <el-option label="张伟" value="张伟" />
          </el-select>
        </el-form-item>
        <div class="filter-actions">
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="primary" @click="queryUnits">查询</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="filteredRows" stripe border data-testid="employer-unit-table">
        <el-table-column type="selection" width="44" />
        <el-table-column label="客户名称" min-width="260">
          <template #default="{ row }">
            <div class="customer-cell">
              <span class="thumb">{{ row.unitName.slice(0, 1) }}</span>
              <div>
                <strong>{{ row.unitName }}</strong>
                <p>{{ row.unitCode }} · {{ row.address }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="customerType" label="客户类型" min-width="120" />
        <el-table-column prop="level" label="客户等级" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.level === 'A级' ? 'primary' : 'success'" effect="light">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusLabel" label="合作状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'warning'" effect="light">
              {{ row.statusLabel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="最近招标时间" min-width="140" />
        <el-table-column prop="contactName" label="业务开发人" min-width="120" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewUnit(row)">查看</el-button>
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteUnit(row.unitCode)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <span>显示 1 到 {{ filteredRows.length }} 条，共 {{ filteredRows.length }} 条记录</span>
        <el-pagination layout="prev, pager, next" :total="filteredRows.length" :page-size="5" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增用工单位' : '编辑用工单位'" width="560px">
      <el-form label-position="top">
        <el-form-item label="单位编号">
          <el-input v-model.trim="unitForm.unitCode" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="单位名称">
          <el-input v-model.trim="unitForm.unitName" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model.trim="unitForm.address" />
        </el-form-item>
        <el-form-item label="客户类型">
          <el-input v-model.trim="unitForm.customerType" />
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="unitForm.level" style="width: 100%">
            <el-option label="A级" value="A级" />
            <el-option label="B级" value="B级" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作状态">
          <el-select v-model="unitForm.status" style="width: 100%">
            <el-option label="已合作" value="ACTIVE" />
            <el-option label="待签约" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务开发人">
          <el-input v-model.trim="unitForm.contactName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUnit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

type EmployerUnitRow = {
  unitCode: string
  unitName: string
  address: string
  customerType: string
  level: 'A级' | 'B级'
  contactName: string
  status: 'ACTIVE' | 'INACTIVE'
  statusLabel: string
  updatedAt: string
}

const rows = ref<EmployerUnitRow[]>([
  {
    unitCode: 'EMP-001',
    unitName: '希尔顿酒店',
    address: '上海市浦东新区世纪大道8号',
    customerType: '五星级酒店',
    level: 'A级',
    contactName: '李军',
    status: 'ACTIVE',
    statusLabel: '已合作',
    updatedAt: '2023-06-15',
  },
  {
    unitCode: 'EMP-002',
    unitName: '万豪酒店',
    address: '上海市黄浦区南京东路628号',
    customerType: '五星级酒店',
    level: 'A级',
    contactName: '王芳',
    status: 'ACTIVE',
    statusLabel: '已合作',
    updatedAt: '2023-07-05',
  },
  {
    unitCode: 'EMP-003',
    unitName: '如家快捷酒店',
    address: '上海市徐汇区宜山路455号',
    customerType: '连锁酒店',
    level: 'B级',
    contactName: '张伟',
    status: 'ACTIVE',
    statusLabel: '已合作',
    updatedAt: '2023-05-20',
  },
  {
    unitCode: 'EMP-004',
    unitName: '7天连锁酒店',
    address: '上海市静安区天目东路228号',
    customerType: '连锁酒店',
    level: 'B级',
    contactName: '刘敏',
    status: 'INACTIVE',
    statusLabel: '待签约',
    updatedAt: '2023-06-30',
  },
])

const filters = reactive({
  keyword: '',
  customerType: '',
  level: '',
  status: '',
  owner: '',
})

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const unitForm = reactive<EmployerUnitRow>({
  unitCode: '',
  unitName: '',
  address: '',
  customerType: '',
  level: 'A级',
  contactName: '',
  status: 'ACTIVE',
  statusLabel: '已合作',
  updatedAt: '',
})

const filteredRows = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  return rows.value.filter((row) => {
    const matchesKeyword =
      !keyword ||
      row.unitName.toLowerCase().includes(keyword) ||
      row.unitCode.toLowerCase().includes(keyword)
    const matchesStatus = !filters.status || row.status === filters.status
    const matchesType = !filters.customerType || row.customerType === filters.customerType
    const matchesLevel = !filters.level || row.level === filters.level
    const matchesOwner = !filters.owner || row.contactName === filters.owner
    return matchesKeyword && matchesStatus && matchesType && matchesLevel && matchesOwner
  })
})

function nowDate() {
  const date = new Date()
  const yyyy = date.getFullYear()
  const MM = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  return `${yyyy}-${MM}-${dd}`
}

function queryUnits() {
  ElMessage.success('已按筛选条件查询')
}

function resetFilters() {
  filters.keyword = ''
  filters.customerType = ''
  filters.level = ''
  filters.status = ''
  filters.owner = ''
  ElMessage.info('已重置筛选条件')
}

function importUnits() {
  ElMessage.info('导入功能待接入文件服务')
}

function viewUnit(row: EmployerUnitRow) {
  ElMessage.info(`当前查看：${row.unitName}`)
}

function openCreateDialog() {
  dialogMode.value = 'create'
  unitForm.unitCode = `EMP-${String(rows.value.length + 1).padStart(3, '0')}`
  unitForm.unitName = ''
  unitForm.address = ''
  unitForm.customerType = '五星级酒店'
  unitForm.level = 'A级'
  unitForm.contactName = ''
  unitForm.status = 'ACTIVE'
  unitForm.statusLabel = '已合作'
  unitForm.updatedAt = nowDate()
  dialogVisible.value = true
}

function openEditDialog(row: EmployerUnitRow) {
  dialogMode.value = 'edit'
  unitForm.unitCode = row.unitCode
  unitForm.unitName = row.unitName
  unitForm.address = row.address
  unitForm.customerType = row.customerType
  unitForm.level = row.level
  unitForm.contactName = row.contactName
  unitForm.status = row.status
  unitForm.statusLabel = row.statusLabel
  unitForm.updatedAt = row.updatedAt
  dialogVisible.value = true
}

function saveUnit() {
  if (!unitForm.unitCode || !unitForm.unitName || !unitForm.contactName) {
    ElMessage.error('请完整填写单位信息')
    return
  }

  unitForm.statusLabel = unitForm.status === 'ACTIVE' ? '已合作' : '待签约'

  if (dialogMode.value === 'create') {
    const exists = rows.value.some((item) => item.unitCode === unitForm.unitCode)
    if (exists) {
      ElMessage.error('单位编号已存在')
      return
    }
    rows.value.unshift({ ...unitForm, updatedAt: nowDate() })
    ElMessage.success('用工单位新增成功')
  } else {
    const index = rows.value.findIndex((item) => item.unitCode === unitForm.unitCode)
    if (index >= 0) {
      rows.value[index] = { ...unitForm, updatedAt: nowDate() }
      ElMessage.success('用工单位更新成功')
    }
  }

  dialogVisible.value = false
}

function deleteUnit(unitCode: string) {
  rows.value = rows.value.filter((item) => item.unitCode !== unitCode)
  ElMessage.success('用工单位已删除')
}
</script>

<style scoped>
.employer-unit-page {
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

.filter-card,
.table-card {
  margin-top: 14px;
  border-radius: 12px;
  border: 1px solid #edf0f6;
}

.filter-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(170px, 1fr));
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

.customer-cell {
  display: grid;
  grid-template-columns: 32px 1fr;
  align-items: center;
  gap: 10px;
}

.thumb {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #bfdbfe, #dbeafe);
  color: #1e40af;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.customer-cell strong {
  color: #1f2937;
}

.customer-cell p {
  margin: 4px 0 0;
  color: #9ca3af;
  font-size: 12px;
}

.pagination-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 1180px) {
  .filter-form {
    grid-template-columns: repeat(2, minmax(170px, 1fr));
  }
}

@media (max-width: 760px) {
  .employer-unit-page {
    padding: 14px;
  }

  .page-head {
    flex-direction: column;
  }

  .filter-form {
    grid-template-columns: 1fr;
  }

  .pagination-wrap {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
