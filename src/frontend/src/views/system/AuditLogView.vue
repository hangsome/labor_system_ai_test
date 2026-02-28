<template>
  <div class="audit-log-view" data-testid="audit-log-view">
    <header class="page-header">
      <div>
        <h1>审计日志中心</h1>
        <p>查询关键写操作日志，支持按操作人、模块与时间窗口筛选</p>
      </div>
      <el-button type="primary" @click="exportLogs">导出日志</el-button>
    </header>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" class="filter-form" data-testid="audit-filter-form">
        <el-form-item label="操作人">
          <el-input v-model.trim="filters.operator" placeholder="请输入操作人" clearable />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="filters.bizType" placeholder="全部" clearable style="width: 180px">
            <el-option label="角色权限" value="ROLE_PERMISSION" />
            <el-option label="线索管理" value="LEAD" />
            <el-option label="合同管理" value="CONTRACT" />
          </el-select>
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="filters.result" placeholder="全部" clearable style="width: 150px">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.range"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="primary" @click="queryLogs">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="table-header">
          <h3>审计日志列表</h3>
          <span>共 {{ filteredRows.length }} 条</span>
        </div>
      </template>

      <el-table :data="pagedRows" border stripe data-testid="audit-log-table">
        <el-table-column prop="createdAt" label="操作时间" min-width="180" />
        <el-table-column prop="operatorName" label="操作人" min-width="130" />
        <el-table-column label="动作" min-width="120">
          <template #default="{ row }">
            {{ getActionText(row.actionType) }}
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="模块" min-width="150" />
        <el-table-column prop="targetId" label="目标ID" min-width="140" />
        <el-table-column label="结果" min-width="120">
          <template #default="{ row }">
            <el-tag :type="getAuditResultTag(row.result)" effect="light">
              {{ getAuditResultText(row.result) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <span>第 {{ pageNo }} / {{ totalPages }} 页</span>
        <el-pagination
          v-model:current-page="pageNo"
          layout="prev, pager, next"
          :total="filteredRows.length"
          :page-size="pageSize"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { auditResultTagMap, auditResultTextMap } from '../../constants/status'

type AuditResultCode = 'SUCCESS' | 'FAILED'
type AuditActionCode = 'CREATE' | 'UPDATE' | 'TERMINATE'

type AuditRow = {
  createdAt: string
  operatorName: string
  actionType: AuditActionCode
  targetType: string
  targetId: string
  result: AuditResultCode
  bizType: string
}

const actionTextMap: Record<AuditActionCode, string> = {
  CREATE: '新增',
  UPDATE: '修改',
  TERMINATE: '终止',
}

const rows = ref<AuditRow[]>([
  {
    createdAt: '2026-02-27 10:18:20',
    operatorName: '张明',
    actionType: 'UPDATE',
    targetType: '角色权限',
    targetId: 'R202601002',
    result: 'SUCCESS',
    bizType: 'ROLE_PERMISSION',
  },
  {
    createdAt: '2026-02-27 09:46:13',
    operatorName: '王磊',
    actionType: 'CREATE',
    targetType: '线索管理',
    targetId: 'LEAD-001',
    result: 'SUCCESS',
    bizType: 'LEAD',
  },
  {
    createdAt: '2026-02-27 09:20:41',
    operatorName: '李婷',
    actionType: 'TERMINATE',
    targetType: '合同管理',
    targetId: 'LC-2026-003',
    result: 'FAILED',
    bizType: 'CONTRACT',
  },
])

const filters = reactive({
  operator: '',
  bizType: '',
  result: '' as '' | AuditResultCode,
  range: [] as string[],
})

const pageNo = ref(1)
const pageSize = 5

const filteredRows = computed(() => {
  return rows.value.filter((item) => {
    const operator = filters.operator.trim().toLowerCase()
    const matchOperator = !operator || item.operatorName.toLowerCase().includes(operator)
    const matchBizType = !filters.bizType || item.bizType === filters.bizType
    const matchResult = !filters.result || item.result === filters.result

    const range = filters.range
    const datePart = item.createdAt.slice(0, 10)
    let matchRange = true
    if (range.length === 2) {
      const [startDate, endDate] = range
      if (startDate && endDate) {
        matchRange = datePart >= startDate && datePart <= endDate
      }
    }

    return matchOperator && matchBizType && matchResult && matchRange
  })
})

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredRows.value.length / pageSize))
})

const pagedRows = computed(() => {
  const start = (pageNo.value - 1) * pageSize
  return filteredRows.value.slice(start, start + pageSize)
})

watch(
  () => [filters.operator, filters.bizType, filters.result, filters.range],
  () => {
    pageNo.value = 1
  }
)

function resetFilters() {
  filters.operator = ''
  filters.bizType = ''
  filters.result = ''
  filters.range = []
  ElMessage.info('已重置筛选条件')
}

function queryLogs() {
  ElMessage.success(`已查询到 ${filteredRows.value.length} 条日志`)
}

function exportLogs() {
  ElMessage.success(`已导出 ${filteredRows.value.length} 条日志`)
}

function getActionText(action: string) {
  return actionTextMap[action as AuditActionCode] ?? action
}

function getAuditResultText(result: string) {
  return auditResultTextMap[result as AuditResultCode] ?? result
}

function getAuditResultTag(result: string) {
  return auditResultTagMap[result as AuditResultCode] ?? 'info'
}
</script>

<style scoped>
.audit-log-view {
  padding: 22px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.page-header h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.15;
  color: #1f2937;
}

.page-header p {
  margin: 8px 0 0;
  color: #6b7280;
}

.filter-card {
  margin-top: 14px;
  border-radius: 12px;
  border: 1px solid #edf0f6;
}

.filter-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(180px, 1fr));
  gap: 10px 12px;
}

:deep(.filter-form .el-form-item) {
  margin-bottom: 0;
}

.table-card {
  margin-top: 14px;
  border-radius: 12px;
  border: 1px solid #edf0f6;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header h3 {
  margin: 0;
  font-size: 18px;
}

.table-header span {
  color: #64748b;
  font-size: 13px;
}

.pager-wrap {
  margin-top: 12px;
  padding: 0 8px 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 1080px) {
  .filter-form {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }
}

@media (max-width: 720px) {
  .audit-log-view {
    padding: 14px;
  }

  .page-header {
    flex-direction: column;
  }

  .filter-form {
    grid-template-columns: 1fr;
  }

  .pager-wrap {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
