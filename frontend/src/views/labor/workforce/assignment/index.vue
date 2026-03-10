<template>
  <GiPageLayout>
    <GiTable
      row-key="id"
      :data="dataList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :scroll="{ x: '100%', y: '100%', minWidth: 1180 }"
      :disabled-tools="['size']"
      @refresh="search"
    >
      <template #toolbar-left>
        <a-input-search
          v-model="queryForm.description"
          placeholder="搜索岗位/职级"
          allow-clear
          style="width: 220px"
          @search="search"
        />
        <a-input-number
          v-model="queryForm.contractId"
          :min="1"
          placeholder="合同ID"
          style="width: 140px"
          @change="search"
        />
        <a-input-number
          v-model="queryForm.employeeId"
          :min="1"
          placeholder="员工ID"
          style="width: 140px"
          @change="search"
        />
        <a-select
          v-model="queryForm.status"
          :options="ASSIGNMENT_STATUS_OPTIONS"
          placeholder="派遣状态"
          allow-clear
          style="width: 150px"
          @change="search"
        />
        <a-button @click="reset">
          <template #icon><icon-refresh /></template>
          <template #default>重置</template>
        </a-button>
      </template>
      <template #toolbar-right>
        <a-button v-permission="['labor:assignment:create']" type="primary" @click="onAdd">
          <template #icon><icon-plus /></template>
          <template #default>新增派遣</template>
        </a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="ASSIGNMENT_STATUS_COLOR_MAP[record.status] ?? 'arcoblue'">
          {{ getAssignmentStatusLabel(record.status) }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a-link v-permission="['labor:assignment:update']" @click="onUpdate(record)">修改</a-link>
          <a-link v-permission="['labor:assignment:delete']" status="danger" @click="onDelete(record)">删除</a-link>
        </a-space>
      </template>
    </GiTable>

    <a-modal
      v-model:visible="formVisible"
      :title="formMode === 'create' ? '新增派遣关系' : '修改派遣关系'"
      :ok-loading="saveLoading"
      width="680px"
      @ok="onSave"
      @cancel="onCancel"
    >
      <a-form ref="formRef" :model="formState" auto-label-width>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="contractId" label="合同ID" :rules="[{ required: true, message: '请输入合同ID' }]">
              <a-input-number v-model="formState.contractId" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="employeeId" label="员工ID" :rules="[{ required: true, message: '请输入员工ID' }]">
              <a-input-number v-model="formState.employeeId" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="positionName" label="岗位名称">
              <a-input v-model="formState.positionName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="levelName" label="职级">
              <a-input v-model="formState.levelName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="assignedAt" label="派遣日期" :rules="[{ required: true, message: '请选择派遣日期' }]">
              <a-date-picker v-model="formState.assignedAt" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="unassignedAt" label="撤场日期">
              <a-date-picker v-model="formState.unassignedAt" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </GiPageLayout>
</template>

<script setup lang="ts">
import type { FormInstance, TableInstance } from '@arco-design/web-vue'
import { Message } from '@arco-design/web-vue'
import {
  type AssignmentQuery,
  type AssignmentReq,
  type AssignmentResp,
  addAssignment,
  deleteAssignment,
  listAssignment,
  updateAssignment,
} from '@/apis/labor/assignment'
import {
  ASSIGNMENT_STATUS_COLOR_MAP,
  ASSIGNMENT_STATUS_LABEL_MAP,
  ASSIGNMENT_STATUS_OPTIONS,
  toLaborLabel,
} from '@/constant/labor'
import { useTable } from '@/hooks'
import has from '@/utils/has'

defineOptions({ name: 'LaborAssignment' })

const getAssignmentStatusLabel = (value?: string) => toLaborLabel(ASSIGNMENT_STATUS_LABEL_MAP, value)

const queryForm = reactive<AssignmentQuery>({
  sort: ['updateTime,desc'],
})

const {
  tableData: dataList,
  loading,
  pagination,
  search,
  handleDelete,
} = useTable((page) => listAssignment({ ...queryForm, ...page }), { immediate: true })

const columns: TableInstance['columns'] = [
  {
    title: '序号',
    width: 66,
    align: 'center',
    render: ({ rowIndex }) => h('span', {}, rowIndex + 1 + (pagination.current - 1) * pagination.pageSize),
  },
  { title: '合同ID', dataIndex: 'contractId', width: 100 },
  { title: '员工ID', dataIndex: 'employeeId', width: 100 },
  { title: '岗位名称', dataIndex: 'positionName', width: 160, ellipsis: true, tooltip: true },
  { title: '职级', dataIndex: 'levelName', width: 120 },
  { title: '派遣日期', dataIndex: 'assignedAt', width: 120 },
  { title: '撤场日期', dataIndex: 'unassignedAt', width: 120 },
  { title: '状态', dataIndex: 'status', slotName: 'status', width: 110, align: 'center' },
  { title: '更新时间', dataIndex: 'updateTime', width: 180 },
  {
    title: '操作',
    dataIndex: 'action',
    slotName: 'action',
    width: 160,
    align: 'center',
    fixed: 'right',
    show: has.hasPermOr(['labor:assignment:update', 'labor:assignment:delete']),
  },
]

const formVisible = ref(false)
const saveLoading = ref(false)
const formMode = ref<'create' | 'update'>('create')
const formId = ref<string | number>('')
const formRef = ref<FormInstance>()

const getDefaultFormState = (): AssignmentReq => ({
  contractId: 1,
  employeeId: 1,
  positionName: '',
  levelName: '',
  assignedAt: '',
  unassignedAt: '',
})

const formState = reactive<AssignmentReq>(getDefaultFormState())

const reset = () => {
  queryForm.description = undefined
  queryForm.contractId = undefined
  queryForm.employeeId = undefined
  queryForm.status = undefined
  search()
}

const onAdd = () => {
  formMode.value = 'create'
  formId.value = ''
  Object.assign(formState, getDefaultFormState())
  formVisible.value = true
}

const onUpdate = (record: AssignmentResp) => {
  formMode.value = 'update'
  formId.value = record.id
  Object.assign(formState, {
    contractId: Number(record.contractId),
    employeeId: Number(record.employeeId),
    positionName: record.positionName,
    levelName: record.levelName,
    assignedAt: record.assignedAt,
    unassignedAt: record.unassignedAt || '',
  })
  formVisible.value = true
}

const onCancel = () => {
  formVisible.value = false
  formRef.value?.clearValidate()
}

const onSave = async () => {
  const error = await formRef.value?.validate()
  if (error) return false

  saveLoading.value = true
  try {
    if (formMode.value === 'create') {
      await addAssignment(formState)
    } else {
      await updateAssignment(formState, formId.value)
    }
    Message.success('保存成功')
    formVisible.value = false
    search()
    return true
  } finally {
    saveLoading.value = false
  }
}

const onDelete = (record: AssignmentResp) => {
  return handleDelete(() => deleteAssignment(record.id), {
    content: `是否确定删除员工 ${record.employeeId} 的派遣关系？`,
    showModal: true,
  })
}
</script>

<style scoped lang="scss"></style>
